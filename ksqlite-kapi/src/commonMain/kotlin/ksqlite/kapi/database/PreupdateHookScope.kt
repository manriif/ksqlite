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
package ksqlite.kapi.database

import ksqlite.kapi.value.ProtectedValue

/**
 * Scope for use with [PreupdateHook.apply].
 */
public interface PreupdateHookScope {

    /**
     * Number of columns in the row that is being inserted, updated, or deleted.
     */
    public val count: Int

    /**
     * Deepness of the current change within trigger execution.
     */
    public val depth: Int

    /**
     * Returns the index if the column for the blob being written.
     */
    public val blobColumnIndex: Int

    /**
     * Returns the value of the column at [index] of the table row before it is updated.
     *
     * @throws ksqlite.kapi.SQLiteException if no value could be obtained.
     */
    public fun oldValue(index: Int): ProtectedValue

    /**
     * Returns the value of the column at [index] of the table row after it is updated.
     *
     * @throws ksqlite.kapi.SQLiteException if no value could be obtained.
     */
    public fun newValue(index: Int): ProtectedValue
}