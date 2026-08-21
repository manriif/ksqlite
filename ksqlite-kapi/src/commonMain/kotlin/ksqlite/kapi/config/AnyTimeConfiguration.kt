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
 * The subset of SQLite's [configuration options](https://sqlite.org/c3ref/c_config_covering_index_scan.html)
 * that can be read or changed at any point, exposed as [ksqlite.kapi.SQLite.config]. See
 * [ConfigurationScope] for the larger set only available before initialization.
 */
public interface AnyTimeConfiguration {

    /**
     * Number of extra bytes SQLite reserves per page for the page cache the application supplies.
     */
    public val pageCacheHeaderSize: Int

    /**
     * Sets the [Logger] that receives SQLite's internal log messages, or `null` to stop logging.
     *
     * @throws ksqlite.kapi.SQLiteException if setting it fails.
     */
    public fun setLogger(logger: Logger?)
}