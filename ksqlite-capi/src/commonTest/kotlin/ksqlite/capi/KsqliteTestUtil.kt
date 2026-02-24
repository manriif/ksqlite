package ksqlite.capi

import kotlinx.coroutines.test.runTest

/**
 * Initializes SQLite for synchronous testing.
 */
internal expect suspend fun initializeSqliteForSynchronousTest()

/**
 * Initializes SQLite and invokes [block]
 */
internal fun sqliteTest(block: () -> Unit) = runTest {
    initializeSqliteForSynchronousTest()
    block()
}