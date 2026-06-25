@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val Initialized = AtomicBoolean(false)

/**
 * Loads the SQLite library for synchronous testing, without initializing it.
 * Returns `true` if the platform is web, `false` otherwise.
 */
internal expect suspend fun loadSqliteForTest(): Boolean

/**
 * Initializes SQLite and returns whether it has been initialized on web.
 */
internal suspend fun loadAndInitializeSqliteForTest(): Boolean {
    val isWeb = loadSqliteForTest()

    if (Initialized.compareAndSet(expectedValue = false, newValue = true)) {
        assertEquals(
            expected = SqliteResultCode.OK,
            actual = sqlite3_initialize(),
            message = "Failed to initialize SQLite"
        )
    }

    return isWeb
}

/**
 * Initializes SQLite inside a [runTest] scope and invokes [block].
 */
internal fun runSqliteTest(block: suspend TestScope.(isWeb: Boolean) -> Unit) = runTest {
    val isWeb = loadAndInitializeSqliteForTest()

    try {
        block(isWeb)
    } catch (cause: Throwable) {
        if (isWeb) {
            // Give time to the tester to open devtools and see what happened
            cause.printStackTrace()
            awaitCancellation()
        } else {
            throw cause
        }
    }
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
    val outDb = SqliteOutputParam()

    val openResult = sqlite3_open_v2(
        fileName = "test_connection",
        outDb = outDb,
        flags = SqliteOpenFlag.READWRITE or SqliteOpenFlag.MEMORY,
        vfs = null
    )

    assertEquals(SqliteResultCode.OK, openResult)
    block(assertNotNull(outDb.value))
}

/**
 * Opens a connection, creates a test table that have a column for each non-null data type, and
 * passes the connection to [block].
 */
internal fun runSqliteConnectionDataTest(
    block: suspend TestScope.(connection: sqlite3) -> Unit
) = runSqliteConnectionTest { connection ->
    val sql = """
            CREATE table test(
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