package ksqlite.capi

import kotlinx.coroutines.test.runTest
import kotlin.time.Duration

/**
 * Initializes SQLite for synchronous testing.
 */
internal expect suspend fun initializeSqliteForSynchronousTest()

/**
 * Initializes SQLite and invokes [block]
 * TODO remove timeout
 */
internal fun sqliteTest(block: () -> Unit) = runTest(timeout = Duration.INFINITE) {
    initializeSqliteForSynchronousTest()
    block()
}