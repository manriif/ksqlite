@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi

import kotlinx.coroutines.test.runTest
import ksqlite.types.SqliteResultCode
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.assertEquals

private val Initialized = AtomicBoolean(false)

/**
 * Loads the SQLite library for synchronous testing.
 * Returns `true` if the platform is web, `false` otherwise.
 */
internal expect suspend fun loadSqliteForSynchronousTest(): Boolean

/**
 * Initializes SQLite and invokes [block]
 */
internal fun sqliteTest(block: (isWeb: Boolean) -> Unit) = runTest {
    val isWeb = loadSqliteForSynchronousTest()

    if (Initialized.compareAndSet(expectedValue = false, newValue = true)) {
        assertEquals(
            expected = SqliteResultCode.OK,
            actual = sqlite3_initialize(),
            message = "Failed to initialize SQLite"
        )
    }

    block(isWeb)
}