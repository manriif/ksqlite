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
import ksqlite.types.vtab.SqliteVtabCursor

/**
 * Every virtual table module implementation uses a subclass of the following structure to describe
 * cursors that point into the virtual table and are used to loop through the virtual table. Cursors
 * are created using the xOpen method of the module and are destroyed by the xClose method. Cursors
 * are used by the xFilter, xNext, xEof, xColumn, and xRowid methods of the module. Each module
 * implementation will define the content of a cursor structure to suit its own needs.
 *
 * This superclass exists in order to define fields of the cursor that are common to all
 * implementations.
 *
 * [sqlite3_vtab_cursor](https://sqlite.org/c3ref/vtab_cursor.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * Subclasser may pass the typed [sqlite3_vtab] as a constructor parameter if necessary.
 */
public expect open class sqlite3_vtab_cursor() : ClosableStruct, SqliteVtabCursor