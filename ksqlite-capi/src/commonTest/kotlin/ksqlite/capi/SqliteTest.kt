@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vfs.xDelete
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val Initialized = AtomicBoolean(false)

/**
 * Loads the SQLite library for synchronous testing, without initializing it.
 * Returns `true` if the platform uses SQLite WASM, `false` otherwise.
 */
internal expect suspend fun loadSqliteForTest(): Boolean

/**
 * Initializes already loaded SQLite.
 */
internal fun initializeSqliteForTest() {
    if (Initialized.compareAndSet(expectedValue = false, newValue = true)) {
        assertEquals(
            expected = SqliteResultCode.OK,
            actual = sqlite3_initialize(),
            message = "Failed to initialize SQLite"
        )
    }
}

/**
 * Loads SQLite and runs [block] without initializing SQLite.
 */
internal fun runTestNoInit(block: suspend TestScope.(isWasm: Boolean) -> Unit) = runTest {
    val isWasm = loadSqliteForTest()

    try {
        block(isWasm)
    } catch (cause: Throwable) {
        if (isWasm) {
            // Give time for the tester to open devtools and see what happened
            cause.printStackTrace()
            awaitCancellation()
        } else {
            throw cause
        }
    }
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 */
internal fun runSqliteTest(
    block: suspend TestScope.(isWasm: Boolean) -> Unit
) = runTestNoInit { isWasm ->
    initializeSqliteForTest()
    block(isWasm)
}

///////////////////////////////////////////////////////////////////////////
// Connection
///////////////////////////////////////////////////////////////////////////

/**
 * Opens a connection and pass it to [block].
 */
internal fun runSqliteConnectionTest(
    block: suspend TestScope.(connection: sqlite3) -> Unit
) = runSqliteTest {
    val outDb = sqlite3.OutputParam()

    val openResult = sqlite3_open_v2(
        fileName = "test_connection",
        outDb = outDb,
        flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
        vfs = null
    )

    assertEquals(SqliteResultCode.OK, openResult)

    val db = assertNotNull(outDb.value)
    block(db)

    val closeResult = sqlite3_close(db)
    assertEquals(SqliteResultCode.OK, closeResult)
}

/**
 * Opens a connection, creates a test table that have a column for each non-null data type, and
 * passes the connection to [block].
 */
internal fun runSqliteConnectionDataTest(
    block: suspend TestScope.(connection: sqlite3) -> Unit
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
    assertEquals(SqliteResultCode.OK, result)

    block(connection)
}

///////////////////////////////////////////////////////////////////////////
// Files
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the path to [subdirectory] against the OS temporary directory.
 * The returned directory is created if it does not exist and must be writable.
 */
internal expect fun temporaryTestDirectory(subdirectory: String): String

/**
 * Returns the path to the temporary test directory.
 */
internal fun ksqliteTemporaryTestDirectory(): String = temporaryTestDirectory("ksqlite-test")

/**
 * Returns the path to the temporary test file named after [fileName].
 */
internal fun ksqliteTemporaryTestFile(fileName: String): String =
    "${ksqliteTemporaryTestDirectory()}/$fileName"

/**
 * Invokes [block] passing it the path to a temporary file, named after [fileName], which isn't
 * created.
 * The file whose path is passed to [block] must be managed by this [sqlite3_vfs].
 * This [sqlite3_vfs] is used to delete the file, before and after [block] invocation.
 */
internal fun <R> sqlite3_vfs.usingRealTempFile(
    fileName: String,
    block: (path: String) -> R
): R {
    val path = ksqliteTemporaryTestFile(fileName)

    fun deleteFile() {
        val deleteResult = xDelete(fileName, 0)

        assertTrue(
            deleteResult == SqliteResultCode.OK
                    || deleteResult == SqliteResultCode.IOERR.DELETE_NOENT
                    || deleteResult == SqliteResultCode.IOERR.DELETE // Android JNI, why ?
        )
    }

    deleteFile()

    return try {
        block(path)
    } finally {
        deleteFile()
    }
}