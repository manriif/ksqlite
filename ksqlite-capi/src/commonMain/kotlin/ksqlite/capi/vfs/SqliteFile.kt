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
import ksqlite.types.vfs.SqliteFile

/**
 * An sqlite3_file object represents an open file in the OS interface layer. Individual OS interface
 * implementations will want to subclass this object by appending additional fields for their own
 * use. The pMethods entry is a pointer to an sqlite3_io_methods object that defines methods for
 * performing I/O operations on the open file.
 *
 * [sqlite3_file](https://sqlite.org/c3ref/file.html)
 */
public expect class sqlite3_file(vfs: sqlite3_vfs) : ClosableStruct, SqliteFile {

    public val pMethods: sqlite3_io_methods?
}