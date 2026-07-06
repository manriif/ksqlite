package ksqlite.capi.vtab

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_declare_vtab
import ksqlite.capi.sqlite3_result_int64
import ksqlite.capi.sqlite3_result_null
import ksqlite.capi.sqlite3_result_text
import ksqlite.capi.sqlite3_value_int64
import ksqlite.capi.sqlite3_value_text
import ksqlite.capi.sqlite3_value_type
import ksqlite.capi.sqlite3_vtab_config
import ksqlite.capi.sqlite3_vtab_nochange
import ksqlite.capi.sqlite3_vtab_on_conflict
import ksqlite.capi.vtab.callbacks.SqliteVtabCreateOrConnectCallback
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteDataType
import ksqlite.types.vtab.SqliteModuleVersion
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteVtabScanFlag.UNIQUE
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

///////////////////////////////////////////////////////////////////////////
// Generated with Claude help, was too lazy to write whole virtual table tests
///////////////////////////////////////////////////////////////////////////

/**
 * Minimal in-memory virtual table over a single (id, name) row store. SQLite's own
 * correctness isn't under test here — only that every ksqlite virtual table callback is
 * wired through correctly in both directions, including real xSavepoint/xRollback undo
 * semantics so the tests can assert on actual resulting data, not just call counts.
 */
private class KvVtab : sqlite3_vtab() {
    var db: sqlite3? = null
    val rows = mutableMapOf<Long, String>()
    var nextRowid = 1L
    var transactionSnapshot: Map<Long, String>? = null
    val savepoints = mutableMapOf<Int, Map<Long, String>>()
}

private class KvCursor(val owner: KvVtab) : sqlite3_vtab_cursor() {
    var rowids: List<Long> = emptyList()
    var position = 0
}

private const val IDX_FULL_SCAN = 0
private const val IDX_POINT_LOOKUP = 1

/**
 * Records every callback invocation so tests can assert on call counts.
 */
internal class KvModuleRecorder {
    var createOrConnectCallCount = 0
    var bestIndexCallCount = 0
    var lastConstraintCount = 0
    var openCallCount = 0
    var closeCallCount = 0
    var filterCallCount = 0
    var nextCallCount = 0
    var columnCallCount = 0
    var rowidCallCount = 0
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
    var findFunctionCallCount = 0
}

internal fun createKvTabModule(
    recorder: KvModuleRecorder,
    appDataCheck: Int = 0
): sqlite3_module<Int> {
    val createOrConnect = SqliteVtabCreateOrConnectCallback { db, appData, _ ->
        assertEquals(appDataCheck, appData)
        recorder.createOrConnectCallCount++

        val sql = "CREATE TABLE x(id INTEGER PRIMARY KEY, name TEXT NOT NULL)"
        val declareResult = sqlite3_declare_vtab(db, sql)

        if (declareResult != OK) {
            failure("declare_vtab failed with $declareResult")
        } else {
            val configResult = sqlite3_vtab_config(db, SqliteVtabConfigOption.INNOCUOUS)

            if (configResult != OK) {
                failure("sqlite3_vtab_config failed with $configResult")
            } else {
                success(KvVtab().apply { this.db = db })
            }
        }
    }

    return sqlite3_module(
        version = SqliteModuleVersion.VERSION_4,
        create = createOrConnect,
        connect = createOrConnect,
        bestIndex = { vTab, info ->
            recorder.bestIndexCallCount++
            recorder.lastConstraintCount = info.nConstraint

            var pointLookupArgIndex = -1

            for (i in 0 until info.nConstraint) {
                if (info.getConstraintUsable(i) != 0 &&
                    info.getConstraintColumn(i) == 0 &&
                    info.getConstraintOp(i) == SqliteVtabConstraintOperatorCode.EQ
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
                info.idxFlags = UNIQUE
            } else {
                info.idxNum = IDX_FULL_SCAN
                info.estimatedCost = vTab.rows.size.toDouble().coerceAtLeast(1.0)
                info.estimatedRows = vTab.rows.size.toLong()
            }

            OK
        },
        disconnect = { _ ->
            recorder.disconnectCallCount++
            OK
        },
        destroy = { _ ->
            recorder.destroyCallCount++
            OK
        },
        open = { vTab ->
            recorder.openCallCount++
            success(KvCursor(vTab))
        },
        close = { _ ->
            recorder.closeCallCount++
            OK
        },
        filter = { cursor, idxNum, _, arguments ->
            recorder.filterCallCount++

            cursor.rowids = if (idxNum == IDX_POINT_LOOKUP) {
                val id = assertNotNull(sqlite3_value_int64(arguments[0]))
                if (cursor.owner.rows.containsKey(id)) listOf(id) else emptyList()
            } else {
                cursor.owner.rows.keys.toList()
            }
            cursor.position = 0

            OK
        },
        next = { cursor ->
            recorder.nextCallCount++
            cursor.position++
            OK
        },
        eof = { cursor ->
            if (cursor.position >= cursor.rowids.size) 1 else 0
        },
        column = { cursor, context, columnIndex ->
            recorder.columnCallCount++
            val rowid = cursor.rowids[cursor.position]

            when (columnIndex) {
                0 -> sqlite3_result_int64(context, rowid)
                1 -> if (sqlite3_vtab_nochange(context) != 0) {
                    recorder.nochangeSeenCount++
                    sqlite3_result_null(context)
                } else {
                    sqlite3_result_text(context, cursor.owner.rows.getValue(rowid))
                }

                else -> error("Unexpected column index: $columnIndex")
            }

            OK
        },
        rowid = { cursor ->
            recorder.rowidCallCount++
            success(cursor.rowids[cursor.position])
        },
        update = { vTab, arguments ->
            recorder.updateCallCount++
            recorder.lastConflictMode = sqlite3_vtab_on_conflict(assertNotNull(vTab.db))

            when {
                // nArg == 1: DELETE, argv[0] holds the rowid to remove.
                arguments.size == 1 -> {
                    val rowid = assertNotNull(sqlite3_value_int64(arguments[0]))
                    vTab.rows.remove(rowid)
                    success(null)
                }

                // argv[0] NULL: INSERT. argv[1] is the proposed rowid (NULL = auto-assign),
                // argv[2]/argv[3] are the id/name column values.
                sqlite3_value_type(arguments[0]) == SqliteDataType.NULL -> {
                    val proposedRowid = sqlite3_value_int64(arguments[1])
                        .takeIf { sqlite3_value_type(arguments[1]) != SqliteDataType.NULL }
                    val rowid = proposedRowid ?: vTab.nextRowid
                    val name = assertNotNull(sqlite3_value_text(arguments[3]))

                    vTab.rows[rowid] = name
                    vTab.nextRowid = maxOf(vTab.nextRowid, rowid + 1)
                    success(rowid)
                }

                // Otherwise: UPDATE. argv[0] is the existing rowid, argv[1] the (possibly new) one.
                else -> {
                    val oldRowid = assertNotNull(sqlite3_value_int64(arguments[0]))
                    val newRowid = assertNotNull(sqlite3_value_int64(arguments[1]))
                    val name = assertNotNull(sqlite3_value_text(arguments[3]))

                    vTab.rows.remove(oldRowid)
                    vTab.rows[newRowid] = name
                    vTab.nextRowid = maxOf(vTab.nextRowid, newRowid + 1)
                    success(newRowid)
                }
            }
        },
        findFunction = { _, _, _ ->
            recorder.findFunctionCallCount++
            doNotOverload()
        },
        begin = { vTab ->
            recorder.beginCallCount++
            vTab.transactionSnapshot = vTab.rows.toMap()
            OK
        },
        sync = { _ ->
            recorder.syncCallCount++
            OK
        },
        commit = { vTab ->
            recorder.commitCallCount++
            vTab.transactionSnapshot = null
            OK
        },
        rollback = { vTab ->
            recorder.rollbackCallCount++
            vTab.transactionSnapshot?.let { snapshot ->
                vTab.rows.clear()
                vTab.rows.putAll(snapshot)
            }
            vTab.transactionSnapshot = null
            OK
        },
        rename = { _, _ ->
            recorder.renameCalled = true
            OK
        },
        savepoint = { vTab, id ->
            recorder.savepointCalls.add(id)
            vTab.savepoints[id] = vTab.rows.toMap()
            OK
        },
        release = { vTab, id ->
            recorder.releaseCalls.add(id)
            vTab.savepoints.keys.removeAll { it >= id }
            OK
        },
        rollbackTo = { vTab, id ->
            recorder.rollbackToCalls.add(id)
            val snapshot = assertNotNull(vTab.savepoints[id])
            vTab.rows.clear()
            vTab.rows.putAll(snapshot)
            vTab.savepoints.keys.removeAll { it > id }
            OK
        },
        integrity = { _, _, _, _ ->
            recorder.integrityCallCount++
            success(null)
        },
    )
}