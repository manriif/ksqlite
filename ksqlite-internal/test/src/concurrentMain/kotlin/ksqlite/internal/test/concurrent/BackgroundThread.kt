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
package ksqlite.internal.test.concurrent

import kotlin.time.Duration

/**
 * A unit of work started on a real, separate thread by [startBackgroundThread].
 */
public expect class BackgroundThread {

    /**
     * Blocks the calling thread until this background thread completes or [timeout] elapses.
     *
     * Returns `true` if the thread completed, `false` if it is still running once [timeout]
     * elapses (for example because of a deadlock).
     */
    public fun join(timeout: Duration): Boolean
}

/**
 * Starts [block] running on a new background thread.
 */
public expect fun startBackgroundThread(block: () -> Unit): BackgroundThread
