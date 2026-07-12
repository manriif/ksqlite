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

package ksqlite.capi.vtab

import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.memory.MemoryScope
import ksqlite.types.vtab.SqliteVtab

/**
 * Every virtual table module implementation uses a subclass of this object to describe a particular
 * instance of the virtual table. Each subclass will be tailored to the specific needs of the module
 * implementation. The purpose of this superclass is to define certain fields that are common to all
 * module implementations.
 *
 * [sqlite3_vtab](https://sqlite.org/c3ref/vtab.html)
 */
public expect open class sqlite3_vtab() : ClosableStruct, MemoryScope, SqliteVtab {

    override val nRef: Int
    override var errMsg: String?
}