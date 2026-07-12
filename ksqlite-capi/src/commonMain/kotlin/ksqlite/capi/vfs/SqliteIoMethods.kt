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

import ksqlite.capi.memory.Struct
import ksqlite.capi.vfs.callbacks.SqliteIoMethodsCloseCallback
import ksqlite.types.SqliteResultCode
import ksqlite.types.vfs.SqliteIoMethods
import ksqlite.types.vfs.SqliteIoMethodsVersion

/**
 * Every file opened by the sqlite3_vfs.xOpen method populates an sqlite3_file object (or, more
 * commonly, a subclass of the sqlite3_file object) with a pointer to an instance of this object.
 * This object defines the methods used to perform various operations against the open file
 * represented by the sqlite3_file object.
 *
 * [sqlite3_io_methods](https://sqlite.org/c3ref/io_methods.html)
 */
public expect class sqlite3_io_methods : Struct, SqliteIoMethods {

    override val iVersion: SqliteIoMethodsVersion

    public val xClose: SqliteIoMethodsCloseCallback
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Applies [xClose] with supplied arguments.
 */
public fun sqlite3_io_methods.xClose(file: sqlite3_file): SqliteResultCode = xClose.apply(file)