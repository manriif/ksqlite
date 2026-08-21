/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.kapi.vtab

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.function.ScalarFunction
import ksqlite.kapi.result.Result
import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteDataType
import ksqlite.types.vtab.SqliteIndexInfo
import kotlin.test.assertNotNull

private const val IDX_FULL_SCAN = 0
private const val IDX_POINT_LOOKUP = 1

/**
 * Records every callback invocation so tests can assert on call counts, mirroring
 * `ksqlite.capi.vtab.KvModuleRecorder`.
 *
 * SQLite's own correctness isn't under test here -- only that every kapi virtual table callback
 * is wired through correctly in both directions, including real savepoint/rollback undo
 * semantics so the tests can assert on actual resulting data, not just call counts.
 */
internal class KvModuleRecorder {
    var createOrConnectCallCount = 0
    var lastConstraintCount = 0
    var updateCallCount = 0
    var nochangeSeenCount = 0
    var lastConflictMode: SqliteConflictResolutionMode? = null
    var renameCalled = false
    var disconnectCallCount = 0
    var destroyCallCount = 0
    var beginCallCount = 0
    var syncCallCount = 0
    var commitCallCount = 0
    var rollbackCallCount = 0
    val savepointCalls = mutableListOf<Int>()
    val releaseCalls = mutableListOf<Int>()
    val rollbackToCalls = mutableListOf<Int>()
    var integrityCallCount = 0
    var findFunctionOfferedCount = 0
    var overloadCallCount = 0
}

/**
 * Minimal in-memory virtual table over a single (id, name) row store.
 */
internal class KvVirtualTable(internal val recorder: KvModuleRecorder) : VirtualTable() {

    val rows = mutableMapOf<Long, String>()
    var nextRowid = 1L
    var transactionSnapshot: Map<Long, String>? = null
    val savepoints = mutableMapOf<Int, Map<Long, String>>()

    override fun VirtualTableBestIndexScope.bestIndex(info: SqliteIndexInfo) {
        recorder.lastConstraintCount = info.nConstraint

        var pointLookupArgIndex = -1

        for (i in 0 until info.nConstraint) {
            if (info.getConstraintUsable(i) != 0 &&
                info.getConstraintColumn(i) == 0 &&
                info.getConstraintOp(i) == EQ
            ) {
                info.setConstraintUsageArgvIndex(i, 1)
                info.setConstraintUsageOmit(i, 1)
                pointLookupArgIndex = i
            }
        }

        if (pointLookupArgIndex >= 0) {
            info.idxNum = IDX_POINT_LOOKUP
            info.estimatedCost = 1.0
            info.estimatedRows = 1L
        } else {
            info.idxNum = IDX_FULL_SCAN
            info.estimatedCost = rows.size.toDouble().coerceAtLeast(1.0)
            info.estimatedRows = rows.size.toLong()
        }
    }

    override fun disconnect() {
        recorder.disconnectCallCount++
    }

    override fun destroy() {
        recorder.destroyCallCount++
    }

    override fun open(): VirtualTableCursor = KvCursor(this)

    override fun VirtualTableUpdateScope.update(arguments: Array<ProtectedValue>): Long? {
        recorder.updateCallCount++
        recorder.lastConflictMode = onConflict

        return when {
            // Single argument: DELETE, arguments[0] holds the rowid to remove.
            arguments.size == 1 -> {
                rows.remove(arguments[0].getAsLong())
                null
            }

            // arguments[0] NULL: INSERT. arguments[1] is the proposed rowid (NULL = auto-assign),
            // arguments[2]/arguments[3] are the id/name column values.
            arguments[0].type == SqliteDataType.NULL -> {
                val explicitId = arguments[2].takeIf { it.type != SqliteDataType.NULL }?.getAsLong()
                val proposedRowid =
                    arguments[1].takeIf { it.type != SqliteDataType.NULL }?.getAsLong()
                val rowid = explicitId ?: proposedRowid ?: nextRowid
                val name = assertNotNull(arguments[3].getAsString())

                if (rows.containsKey(rowid)) {
                    when (onConflict) {
                        SqliteConflictResolutionMode.IGNORE -> null
                        SqliteConflictResolutionMode.REPLACE -> {
                            rows[rowid] = name
                            nextRowid = maxOf(nextRowid, rowid + 1)
                            rowid
                        }

                        else -> throw SQLiteException(
                            ksqlite.types.SqliteResultCode.CONSTRAINT.PRIMARYKEY,
                            "UNIQUE constraint failed"
                        )
                    }
                } else {
                    rows[rowid] = name
                    nextRowid = maxOf(nextRowid, rowid + 1)
                    rowid
                }
            }

            // Otherwise: UPDATE. arguments[0] is the existing rowid, arguments[1] the (possibly
            // new) one.
            else -> {
                val oldRowid = arguments[0].getAsLong()
                val newRowid = arguments[1].getAsLong()
                val newName = arguments[3].takeIf { it.type != SqliteDataType.NULL }?.getAsString()
                val oldName = assertNotNull(rows.remove(oldRowid))
                rows[newRowid] = newName ?: oldName
                nextRowid = maxOf(nextRowid, newRowid + 1)
                newRowid
            }
        }
    }

    override fun VirtualTableFindFunctionScope.findFunction(
        name: String,
        argumentCount: Int
    ): ScalarFunction? {
        if (name != "tag" || argumentCount != 1) {
            return null
        }

        recorder.findFunctionOfferedCount++

        return ScalarFunction {
            val text = assertNotNull(it[0].getAsString())
            recorder.overloadCallCount++
            resultString("vtab:$text")
        }
    }

    override fun begin() {
        recorder.beginCallCount++
        transactionSnapshot = rows.toMap()
    }

    override fun sync() {
        recorder.syncCallCount++
    }

    override fun commit() {
        recorder.commitCallCount++
        transactionSnapshot = null
    }

    override fun rollback() {
        recorder.rollbackCallCount++

        transactionSnapshot?.let { snapshot ->
            rows.clear()
            rows.putAll(snapshot)
        }

        transactionSnapshot = null
    }

    override fun rename(newName: String) {
        recorder.renameCalled = true
    }

    override fun savepoint(id: Int) {
        recorder.savepointCalls.add(id)
        savepoints[id] = rows.toMap()
    }

    override fun release(id: Int) {
        recorder.releaseCalls.add(id)
        savepoints.keys.removeAll { it >= id }
    }

    override fun rollbackTo(id: Int) {
        recorder.rollbackToCalls.add(id)
        val snapshot = assertNotNull(savepoints[id])
        rows.clear()
        rows.putAll(snapshot)
        savepoints.keys.removeAll { it > id }
    }

    override fun VirtualTableIntegrityScope.integrity(
        schema: String,
        tableName: String,
        flags: Int
    ) {
        recorder.integrityCallCount++
    }
}

internal class KvCursor(val owner: KvVirtualTable) : VirtualTableCursor() {

    var rowids: List<Long> = emptyList()
    var position = 0

    override fun eof(): Boolean = position >= rowids.size

    override fun VirtualTableFilterScope.filter(
        idxNum: Int,
        idxStr: String?,
        arguments: Array<ProtectedValue>
    ) {
        rowids = if (idxNum == IDX_POINT_LOOKUP) {
            val id = arguments[0].getAsLong()
            if (owner.rows.containsKey(id)) listOf(id) else emptyList()
        } else {
            owner.rows.keys.toList()
        }

        position = 0
    }

    override fun next() {
        position++
    }

    override fun VirtualTableColumnScope.column(index: Int): Result {
        val rowid = rowids[position]

        return when (index) {
            0 -> resultLong(rowid)
            1 -> if (nochange) {
                owner.recorder.nochangeSeenCount++
                resultNull()
            } else {
                resultString(owner.rows.getValue(rowid))
            }

            else -> error("Unexpected column index: $index")
        }
    }

    override fun rowid(): Long = rowids[position]

    override fun close() = Unit
}

/**
 * Registers a fresh [KvVirtualTable]-backed module named [name] on `this` connection, declaring
 * a `(id INTEGER PRIMARY KEY, name TEXT NOT NULL)` schema, and returns the [KvModuleRecorder]
 * used to observe callback invocations.
 */
internal fun DatabaseConnection.createKvModule(
    name: String = "kv",
    recorder: KvModuleRecorder = KvModuleRecorder()
): KvModuleRecorder {
    val module = object : VirtualTableModule.Eponymous(
        optionalFunctions = VirtualTableOptionalFunction.entries.toSet()
    ) {

        override fun VirtualTableCreateOrConnectScope.connect(
            connection: DatabaseConnection,
            arguments: Array<String>
        ): VirtualTable {
            recorder.createOrConnectCallCount++
            declare("CREATE TABLE x(id INTEGER PRIMARY KEY, name TEXT NOT NULL)")
            config.setInnocuous()
            return KvVirtualTable(recorder)
        }
    }

    val _ = createModule(name, module = module)
    return recorder
}
