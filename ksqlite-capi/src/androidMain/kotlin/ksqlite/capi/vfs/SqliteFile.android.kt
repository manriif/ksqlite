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
@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.memory.wrapOrNull
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.structs.sqlite3_file as s3_file

public actual class sqlite3_file private constructor(private val file: s3_file) :
    ClosableStruct(file, Application),
    SqliteFile {

    public actual constructor(vfs: sqlite3_vfs) : this(s3_file(vfs.szOsFile))

    public actual val pMethods: sqlite3_io_methods? by lazy {
        file.pMethods.wrapOrNull(::sqlite3_io_methods)
    }
}