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
@file:OptIn(ObsoleteWorkersApi::class)

package ksqlite.internal.test.concurrent

import kotlin.native.concurrent.Future
import kotlin.native.concurrent.FutureState
import kotlin.native.concurrent.ObsoleteWorkersApi
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.time.Duration
import kotlin.time.TimeSource

public actual class BackgroundThread(
    private val worker: Worker,
    private val future: Future<Unit>,
) {

    public actual fun join(timeout: Duration): Boolean {
        val deadline = TimeSource.Monotonic.markNow() + timeout

        while (future.state == FutureState.SCHEDULED && deadline.hasNotPassedNow()) {
            // Spin.
        }

        val completed = future.state != FutureState.SCHEDULED
        val _ = worker.requestTermination(processScheduledJobs = completed)

        return completed
    }
}

public actual fun startBackgroundThread(block: () -> Unit): BackgroundThread {
    val worker = Worker.start(name = "ksqlite-test-worker")
    val future = worker.execute(TransferMode.SAFE, { block }) { it() }
    return BackgroundThread(worker, future)
}
