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

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_vtab_config
import ksqlite.capi.vtab.SqliteVtabConfigOption
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck

internal class VirtualTableConfigurationImpl(
    private val db: sqlite3,
    private val scope: CloseableScope
) : VirtualTableConfiguration {

    /**
     * Applies the given configuration [option].
     */
    private fun applyOption(option: SqliteVtabConfigOption) =
        scope.notClosed { sqliteResultCheck(sqlite3_vtab_config(db, option)) }

    override fun setConstraintSupportEnabled(enabled: Boolean) =
        applyOption(SqliteVtabConfigOption.CONSTRAINT_SUPPORT(if (enabled) 1 else 0))

    override fun setInnocuous() =
        applyOption(SqliteVtabConfigOption.INNOCUOUS)

    override fun setDirectOnly() =
        applyOption(SqliteVtabConfigOption.DIRECTONLY)

    override fun setUsesAllSchemas() =
        applyOption(SqliteVtabConfigOption.USES_ALL_SCHEMAS)
}