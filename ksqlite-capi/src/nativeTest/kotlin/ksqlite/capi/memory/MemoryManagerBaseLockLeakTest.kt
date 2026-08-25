@file:OptIn(kotlin.native.concurrent.ObsoleteWorkersApi::class)

package ksqlite.capi.memory

import kotlin.native.concurrent.Worker
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private class TestMemoryManager : MemoryManagerBase() {
    fun registerOnce() {
        val disposable = registerDisposable<Unit, TestDisposable> { id -> TestDisposable(id) }
        check(disposable.appData == Unit)
    }

    private inner class TestDisposable(id: Long) : AutoDisposable<Unit>(id, null) {
        override val appData: Unit = Unit
        override fun release() = Unit
    }
}

class MemoryManagerBaseLockLeakTest {

    @Test
    fun registerDisposableFromASecondThreadDoesNotDeadlock() {
        val manager = TestMemoryManager()

        // Exercise registerDisposable once, on this (the test's own) thread - this is what leaks
        // an extra, unbalanced hold on disposableLock via computeNextDisposableId.
        manager.registerOnce()

        // A *different* thread calling into registerDisposable afterward should complete quickly.
        // If the lock leaked, this blocks forever - so run it on a worker and bound how long we
        // wait, rather than hanging the whole test suite if the bug is present.
        val worker = Worker.start(name = "MemoryManagerBaseLockLeakTest-worker")
        try {
            val future = worker.execute(kotlin.native.concurrent.TransferMode.SAFE, { manager }) { mgr ->
                mgr.registerOnce()
                true
            }

            val deadline = TimeSource.Monotonic.markNow() + 5.seconds
            var completed = false
            while (deadline.hasNotPassedNow()) {
                if (future.state != kotlin.native.concurrent.FutureState.SCHEDULED) {
                    completed = true
                    break
                }
            }

            assertTrue(
                completed,
                "A second thread's registerDisposable() call never completed within 5 seconds - " +
                    "disposableLock appears to still be leaking a hold from computeNextDisposableId().",
            )
        } finally {
            // Don't wait on the termination future's .result here: if the bug reproduced, the
            // worker's own queued job (registerOnce(), stuck on the leaked lock) will never finish,
            // and waiting for it would just trade one hang for another right as we're reporting the
            // failure. processScheduledJobs = false requests termination without waiting for
            // whatever the worker is currently stuck on.
            @Suppress("UNUSED_VARIABLE")
            val terminationFuture = worker.requestTermination(processScheduledJobs = false)
        }
    }
}
