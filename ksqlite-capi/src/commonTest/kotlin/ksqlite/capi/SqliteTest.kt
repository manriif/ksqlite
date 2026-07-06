@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vfs.xAccess
import ksqlite.capi.vfs.xDelete
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode.IOERR.DELETE_NOENT
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
            expected = OK,
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
@Suppress("unused")
internal fun findVfs(isWasm: Boolean): sqlite3_vfs {
    // The default WASM vfs can be used here
    return assertNotNull(sqlite3_vfs_find(null))
}

/**
 * Returns a VFS suitable for WAL related tests.
 * TODO use a VFS that supports WAL on WASM
 */
internal fun findWalVfs(isWasm: Boolean): sqlite3_vfs? {
    val vfsName: String? = if (isWasm) return null else null
    return assertNotNull(sqlite3_vfs_find(vfsName))
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 * The test is only executed if a VFS that 'supports' WAL mode is found.
 */
internal fun runSqliteWalTest(
    block: suspend TestScope.(vfs: sqlite3_vfs) -> Unit
) = runSqliteTest { isWasm ->
    block(findWalVfs(isWasm) ?: return@runSqliteTest)
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 * The test is only executed if a VFS that 'supports' WAL mode is found.
 * The path to [fileName] is passed to [block].
 */
internal fun runSqliteWalFileTest(
    fileName: String,
    block: suspend TestScope.(path: String) -> Unit
) = runSqliteWalTest { vfs ->
    vfs.usingRealTempFile(fileName) { path ->
        block(path)
    }
}