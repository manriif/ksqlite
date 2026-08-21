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
     * Returns `true` if this value's column is unchanged by the current UPDATE, and the virtual
     * table skipped setting a result the last time this column was read through
     * [VirtualTableColumnScope.column], typically because [VirtualTableColumnScope.nochange]
     * reported it as unchanging.
     */
    public val ProtectedValue.nochange: Boolean
}