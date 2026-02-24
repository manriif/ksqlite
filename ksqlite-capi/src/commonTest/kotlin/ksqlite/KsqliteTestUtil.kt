package ksqlite

import kotlinx.coroutines.test.runTest

/**
 * Initializes SQLite for synchronous testing.
 */
expect suspend fun initializeSqliteForSynchronousTest()

/**
 * Initializes SQLite and invokes [block]
 */
fun sqliteTest(block: () -> Unit) = runTest {
    initializeSqliteForSynchronousTest()
    block()
}