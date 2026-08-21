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
package ksqlite.kapi.connection

/**
 * Callback to use with [DatabaseConnection.setBusyHandler].
 */
public fun interface BusyHandler {

    /**
     * Called each time an attempt to access a locked table fails. [count] is the number of times
     * this callback has been invoked for the current locking event, starting at `0`. Returning
     * `0` stops the retries and lets the failing call return immediately, any other value causes
     * it to retry.
     */
    public fun apply(count: Int): Int
}