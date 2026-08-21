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

import ksqlite.kapi.value.ProtectedValue
import ksqlite.types.vtab.SqliteIndexInfo

/**
 * Scope to use with [VirtualTable.bestIndex].
 */
public interface VirtualTableBestIndexScope {

    /**
     * Integer between 0 and 3 that helps determine whether the query is distinct.
     */
    public val distinct: Int

    /**
     * Returns the name of the collation sequence to use for text comparisons on the constraint
     * at [index] of the received [SqliteIndexInfo].
     */
    public fun collation(index: Int): String

    /**
     * Declares to SQLite whether the [index]th constraint is an IN() constraint, and if
     * [handle] is non-zero, that this virtual table intends to iterate its right-hand-side
     * values one at a time through [VirtualTableFilterScope.inFirst] and
     * [VirtualTableFilterScope.inNext] instead of having SQLite expand them upfront. Returns
     * `true` if the constraint is indeed an IN() constraint.
     */
    public fun isIn(
        index: Int,
        handle: Int
    ): Boolean

    /**
     * Returns the right-hand-side value of the [index]th constraint, or `null` if it is not
     * known yet.
     */
    public fun rhsValue(index: Int): ProtectedValue?
}