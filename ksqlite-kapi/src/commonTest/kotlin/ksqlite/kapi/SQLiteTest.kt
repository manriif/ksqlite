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
package ksqlite.kapi

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.sqlite3_vfs_find
import ksqlite.capi.vfs.xAccess
import ksqlite.capi.vfs.xDelete
import ksqlite.internal.test.isWasm
import ksqlite.internal.test.ksqliteTempTestFile
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.types.SqliteResultCode.IOERR.DELETE_NOENT
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Runs [block] without initializing SQLite.
 */
internal fun runTestNoInit(block: () -> Unit) = block()

/**
 * Initializes SQLite, invokes [block] with the resulting [SQLite] instance, then closes it.
 */
internal fun runSqliteTest(
    configure: (ConfigurationScope.() -> Unit)? = null,
    block: (sqlite: SQLite) -> Unit
) = runTestNoInit {
    val sqlite = SQLite.initialize(configure)

    try {
        block(sqlite)
    } finally {
        sqlite.close()
    }
}

///////////////////////////////////////////////////////////////////////////
// Connection
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes SQLite, opens an in-memory [DatabaseConnection] and passes both to [block].
 */
internal fun runSqliteConnectionTest(
    block: (sqlite: SQLite, connection: DatabaseConnection) -> Unit
) = runSqliteTest { sqlite ->
    val connection = sqlite.open(":memory:")

    try {
        block(sqlite, connection)
    } finally {
        connection.close()
    }
}

/**
 * Opens a connection, creates a test table that has a column for each non-null data type, and
 * passes the [SQLite] instance and [DatabaseConnection] to [block].
 */
internal fun runSqliteConnectionDataTest(
    block: (sqlite: SQLite, connection: DatabaseConnection) -> Unit
) = runSqliteConnectionTest { sqlite, connection ->
    connection.execute(
        """
            CREATE TABLE test(
                integer_t INTEGER,
                float_t FLOAT,
                text_t TEXT,
                blob_t BLOB,
                blob2_t BLOB
            );
        """.trimIndent()
    )

    block(sqlite, connection)
}

///////////////////////////////////////////////////////////////////////////
// Files
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] passing it the path to a temporary file, named after [fileName], which isn't
 * created.
 *
 * The default virtual file system is used to delete the file and associated wal/journal files,
 * before and after [block] invocation.
 */
internal inline fun <R> usingRealTempFile(
    fileName: String,
    block: (path: String) -> R
): R {
    val path = ksqliteTempTestFile(fileName)
    val vfs = assertNotNull(sqlite3_vfs_find(null))

    val deleteFile = { message: String ->
        val deleteResult = vfs.xDelete(path, 0)
        assertTrue(deleteResult == OK || deleteResult == DELETE_NOENT)

        val outAccessFlags = Int32OutputParam(-1)
        val accessResult = vfs.xAccess(path, EXISTS, outAccessFlags)
        assertEquals(OK, accessResult)
        assertEquals(0, outAccessFlags.value, message)
    }

    deleteFile("File $path already exists and deletion failed")

    return try {
        block(path)
    } finally {
        deleteFile("Failed to delete file $path")
    }
}

///////////////////////////////////////////////////////////////////////////
// WAL
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes SQLite and invokes [block] with the path to a real temporary file.
 * The test is only executed on platforms that 'support' WAL mode.
 * TODO use a VFS that supports WAL on WASM
 */
internal fun runSqliteWalFileTest(
    fileName: String,
    block: (sqlite: SQLite, path: String) -> Unit
) = runSqliteTest { sqlite ->
    if (isWasm) {
        return@runSqliteTest
    }

    usingRealTempFile(fileName) { path ->
        block(sqlite, path)
    }
}
