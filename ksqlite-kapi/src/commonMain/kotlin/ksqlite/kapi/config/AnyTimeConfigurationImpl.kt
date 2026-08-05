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
package ksqlite.kapi.config

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.sqlite3_config
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam

internal open class AnyTimeConfigurationImpl(protected val scope: CloseableScope) :
    AnyTimeConfiguration {

    override val pageCacheHeaderSize: Int
        get() = usingParam(Int32OutputParam(0)) { applyOption(SqliteConfigOption.PCACHE_HDRSZ(it)) }

    /**
     * Applies the given configuration [option].
     */
    protected fun applyOption(option: SqliteConfigOption) =
        scope.notClosed { sqliteResultCheck(sqlite3_config(option)) }

    override fun setLogger(logger: Logger?) = logger
        ?.let { applyOption(SqliteConfigOption.LOG(it, LoggerCallback)) }
        ?: applyOption(SqliteConfigOption.LOG(null, null))
}