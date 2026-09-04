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
package ksqlite.internal.runtime.concurrency

/**
 * JS and wasmJs are single-threaded. There is nothing to actually lock. Only the closed check
 * remains meaningful.
 */
public actual class SafeLock: AutoCloseable {

    private var closed = false

    public actual val isClosed: Boolean
        get() = closed

    public actual fun lock() {
        check(!closed) { "Lock is closed" }
    }

    public actual fun unlock(): Unit = Unit

    public actual override fun close() {
        closed = true
    }
}
