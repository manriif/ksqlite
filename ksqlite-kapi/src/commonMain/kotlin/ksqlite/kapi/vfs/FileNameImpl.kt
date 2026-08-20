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
package ksqlite.kapi.vfs

import ksqlite.capi.sqlite3_filename
import ksqlite.capi.sqlite3_filename_database
import ksqlite.capi.sqlite3_filename_journal
import ksqlite.capi.sqlite3_filename_wal
import ksqlite.capi.sqlite3_uri_boolean
import ksqlite.capi.sqlite3_uri_int64
import ksqlite.capi.sqlite3_uri_key
import ksqlite.capi.sqlite3_uri_parameter
import ksqlite.internal.runtime.closeable.CloseableScope

internal class FileNameImpl(
    private val filename: sqlite3_filename,
    private val scope: CloseableScope
) : FileName {

    override val content: String
        get() = scope.notClosed { filename.content }

    override val databaseFileName: String?
        get() = scope.notClosed { sqlite3_filename_database(filename) }

    override val journalFileName: String?
        get() = scope.notClosed { sqlite3_filename_journal(filename) }

    override val walFileName: String?
        get() = scope.notClosed { sqlite3_filename_wal(filename) }

    override fun getKey(index: Int): String? =
        scope.notClosed { sqlite3_uri_key(filename, index) }

    override fun geValue(parameter: String): String? =
        scope.notClosed { sqlite3_uri_parameter(filename, parameter) }

    override fun geValue(parameter: String, default: Boolean): Boolean =
        scope.notClosed { sqlite3_uri_boolean(filename, parameter, if (default) 1 else 0) != 0 }

    override fun geValue(parameter: String, default: Long): Long =
        scope.notClosed { sqlite3_uri_int64(filename, parameter, default) }
}