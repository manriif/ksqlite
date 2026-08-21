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
package ksqlite.kapi.vtab

import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode

/**
 * Scope to use with [VirtualTable.findFunction].
 */
public interface VirtualTableFindFunctionScope {

    /**
     * Sets the custom constraint operator code to return to SQLite.
     *
     * By default, if [VirtualTable.findFunction] returns a non-null function, `one` is returned to
     * SQLite to indicate that the function is overloaded, and `zero` is returned otherwise.
     *
     * If a custom [code] is set, it is returned, with the scalar function, instead of the default
     * `one`. However, if no scalar function is returned but a custom [code] is set, then an
     * exception is thrown.
     *
     * Note that this replaces any [code] from a previous call to [customConstraintOperator].
     */
    public fun customConstraintOperator(code: SqliteVtabConstraintOperatorCode.Custom)
}