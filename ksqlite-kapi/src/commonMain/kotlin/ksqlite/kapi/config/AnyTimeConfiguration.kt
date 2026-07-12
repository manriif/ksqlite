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
@file:Suppress("SpellCheckingInspection")

package ksqlite.kapi.config

/**
 * Exposes the anytime options of the SQLite configuration API.
 *
 * [Configurations Options](https://sqlite.org/c3ref/c_config_covering_index_scan.html)
 */
public interface AnyTimeConfiguration {

    /**
     * Number of extra bytes per page required for each page in SQLITE_CONFIG_PAGECACHE.
     */
    public val pageCacheHeaderSize: Int

    /**
     * Sets the logging interface.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setLogger(logger: Logger?)
}