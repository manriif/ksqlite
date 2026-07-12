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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import ksqlite.capi.memory.ClosableStruct
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.sqlite3_file as s3_file

public actual class sqlite3_file
private constructor(override val pointer: CPointer<s3_file>) :
    ClosableStruct(pointer),
    SqliteFile {

    public actual constructor(vfs: sqlite3_vfs) : this(allocate(vfs.szOsFile.toLong()))

    public actual val pMethods: sqlite3_io_methods? by lazy {
        pointer.pointed.pMethods?.let(::sqlite3_io_methods)
    }
}