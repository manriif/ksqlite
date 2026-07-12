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
package ksqlite.kapi.vtab

import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.SqliteConflictResolutionMode

/**
 * Scope to use with [VirtualTable.update].
 */
public interface VirtualTableUpdateScope {

    /**
     * Returns the virtual table conflict policy.
     */
    public val onConflict: SqliteConflictResolutionMode

    /**
     * Returns `true` if and only if the column corresponding to X is unchanged by the UPDATE
     * operation that the xUpdate method call was invoked to implement and if the prior xColumn
     * method call that was invoked to extract the value for that column returned without setting a
     * result (probably because it queried sqlite3_vtab_nochange() and found that the column was
     * unchanging).
     */
    public val ProtectedValue.nochange: Boolean
}