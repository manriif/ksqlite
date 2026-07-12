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
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi

import kotlinx.coroutines.test.runTest
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vfs.xAccess
import ksqlite.capi.vfs.xDelete
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode.IOERR.DELETE_NOENT
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Indicates whether the platform use SQLite WASM.
 */
internal expect val isWasm: Boolean

/**
 * Well... It seems like the Kotlin/JS + Karma + Webpack combo does not like how ksqlite.mjs loads
 * the ksqlite.wasm module (asynchronously). But top-level await is normally a supported feature.
 * Anyway we do not want to have an asynchronous loading of the .wasm in Kotlin as it will require
 * to block the main thread (which is not possible, in non-test context) or we'll have to make all
 * the APIs suspend or force the developer to find a way to wait for ksqlite.Wasm to be
 * asynchronously loaded...
 *
 * Wrapping all tests in a [runTest] reduce the chances to have it working.
 *
 * By the past the .wasm was loaded asynchronously using coroutines and in Kotlin code. That
 * permitted to have the tests running by 100%, but it was a temporary solution.
 *
 * That being said, I suspect a timing issue.
 * Even with that there is a 20% chance test not starts.
 */
@Suppress("unused")
val thereAreSomeDaysImAskingMySelfWhatWouldBeTheGreatTimeToRetireFromProgramming by lazy {
    runTest {
        error("This piece of code is what made tests working on Kotlin/JS (when it want to)...")
    }
}

/**
 * Loads SQLite and runs [block] without initializing SQLite.
 */
internal fun runTestNoInit(block: () -> Unit) = try {
    block()
} catch (cause: Throwable) {
    if (isWasm) {
        // Give time for the tester to open devtools and see what happened
        // TODO remove this, it was useful during early development but now that hard to debug bugs
        //  were fixed it seems no longer necessary
        cause.printStackTrace()
        //awaitCancellation()
    } else {
        throw cause
    }
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 */
internal fun runSqliteTest(block: () -> Unit) = runTestNoInit {
    assertEquals(
        expected = OK,
        actual = sqlite3_initialize(),
        message = "Failed to initialize SQLite"
    )

    block()

    assertEquals(
        expected = OK,
        actual = sqlite3_shutdown(),
        message = "Failed to shutdown SQLite"
    )
}

///////////////////////////////////////////////////////////////////////////
// Connection
///////////////////////////////////////////////////////////////////////////

/**
 * Opens a connection and pass it to [block].
 */
internal fun runSqliteConnectionTest(block: (connection: sqlite3) -> Unit) = runSqliteTest {
    val outDb = sqlite3.OutputParam()

    val openResult = sqlite3_open_v2(
        fileName = "test_connection",
        outDb = outDb,
        flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
        vfs = null
    )

    assertEquals(OK, openResult)

    val db = assertNotNull(outDb.value)
    block(db)

    val closeResult = sqlite3_close(db)
    assertEquals(OK, closeResult)
}

/**
 * Opens a connection, creates a test table that have a column for each non-null data type, and
 * passes the connection to [block].
 */
internal fun runSqliteConnectionDataTest(
    block: (connection: sqlite3) -> Unit
) = runSqliteConnectionTest { connection ->
    val sql = """
            CREATE TABLE test(
                integer_t INTEGER, 
                float_t FLOAT,
                text_t TEXT,
                blob_t BLOB,
                blob2_t BLOB
            );
        """.trimIndent()

    val result = sqlite3_exec(connection, sql, null, null, null)
    assertEquals(OK, result)

    block(connection)
}

///////////////////////////////////////////////////////////////////////////
// Files
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the path to [subdirectory] against the OS temporary directory.
 * The returned directory is created if it does not exist and must be writable.
 */
internal expect fun tempTestDirectory(subdirectory: String): String

/**
 * Returns the path to the temporary test directory.
 */
internal fun ksqliteTempTestDirectory(): String = tempTestDirectory("ksqlite-test")

/**
 * Returns the path to the temporary test file named after [fileName].
 */
internal fun ksqliteTempTestFile(fileName: String): String =
    "${ksqliteTempTestDirectory()}/$fileName"

/**
 * Invokes [block] passing it the path to a temporary file, named after [fileName], which isn't
 * created.
 * The file whose path is passed to [block] must be managed by this [sqlite3_vfs].
 *
 * This [sqlite3_vfs] is used to delete the file and associated wal files, before and after
 * [block] invocation.
 */
internal inline fun <R> sqlite3_vfs.usingRealTempFile(
    fileName: String,
    block: (path: String) -> R
): R {
    val path = ksqliteTempTestFile(fileName)

    val deleteFile = { message: String ->
        val deleteResult = xDelete(path, 0)
        assertTrue(deleteResult == OK || deleteResult == DELETE_NOENT)

        val outAccessFlags = Int32OutputParam(-1)
        val accessResult = xAccess(path, EXISTS, outAccessFlags)
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
// VFS
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a VFS suitable for real temporary file related tests that does not require WAL nor
 * locking.
 */
internal fun findVfs(): sqlite3_vfs {
    // The default WASM vfs can be used here
    return assertNotNull(sqlite3_vfs_find(null))
}

/**
 * Returns a VFS suitable for WAL related tests.
 * TODO use a VFS that supports WAL on WASM
 */
internal fun findWalVfs(): sqlite3_vfs? {
    if (isWasm) {
        return null
    }

    return findVfs()
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 * The test is only executed if a VFS that 'supports' WAL mode is found.
 */
internal fun runSqliteWalTest(block: (vfs: sqlite3_vfs) -> Unit) = runSqliteTest {
    block(findWalVfs() ?: return@runSqliteTest)
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 * The test is only executed if a VFS that 'supports' WAL mode is found.
 * The path to [fileName] is passed to [block].
 */
internal fun runSqliteWalFileTest(
    fileName: String,
    block: (path: String) -> Unit
) = runSqliteWalTest { vfs ->
    vfs.usingRealTempFile(fileName) { path ->
        block(path)
    }
}