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

package ksqlite.types.vtab

/**
 * These macros define the allowed values for the sqlite3_index_info.aConstraint[].op field. Each
 * value represents an operator that is part of a constraint term in the WHERE clause of a query
 * that uses a virtual table.
 *
 * [Virtual Table Constraint Operator Codes](https://sqlite.org/c3ref/c_index_constraint_eq.html).
 */
public sealed class SqliteVtabConstraintOperatorCode(public open val code: Int) {

    /**
     * The `=` operator.
     */
    public data object EQ : SqliteVtabConstraintOperatorCode(2)

    /**
     * The `>` operator.
     */
    public data object GT : SqliteVtabConstraintOperatorCode(4)

    /**
     * The `<=` operator.
     */
    public data object LE : SqliteVtabConstraintOperatorCode(8)

    /**
     * The `<` operator.
     */
    public data object LT : SqliteVtabConstraintOperatorCode(16)

    /**
     * The `>=` operator.
     */
    public data object GE : SqliteVtabConstraintOperatorCode(32)

    /**
     * The `MATCH` operator.
     */
    public data object MATCH : SqliteVtabConstraintOperatorCode(64)

    /**
     * The `LIKE` operator.
     */
    public data object LIKE : SqliteVtabConstraintOperatorCode(65)

    /**
     * The `GLOB` operator.
     */
    public data object GLOB : SqliteVtabConstraintOperatorCode(66)

    /**
     * The `REGEXP` operator.
     */
    public data object REGEXP : SqliteVtabConstraintOperatorCode(67)

    /**
     * The `!=` operator.
     */
    public data object NE : SqliteVtabConstraintOperatorCode(68)

    /**
     * The `IS NOT` operator.
     */
    public data object ISNOT : SqliteVtabConstraintOperatorCode(69)

    /**
     * The `IS NOT NULL` operator.
     */
    public data object ISNOTNULL : SqliteVtabConstraintOperatorCode(70)

    /**
     * The `IS NULL` operator.
     */
    public data object ISNULL : SqliteVtabConstraintOperatorCode(71)

    /**
     * The `IS` operator.
     */
    public data object IS : SqliteVtabConstraintOperatorCode(72)

    /**
     * A LIMIT clause on the query.
     */
    public data object LIMIT : SqliteVtabConstraintOperatorCode(73)

    /**
     * An OFFSET clause on the query.
     */
    public data object OFFSET : SqliteVtabConstraintOperatorCode(74)

    /**
     * Lower bound of the range of codes reserved for function-based constraints returned by the
     * xFindFunction callback. See [Custom].
     */
    public data object FUNCTION : SqliteVtabConstraintOperatorCode(150)

    /**
     * Custom operator code returned by xFindFunction callback.
     */
    public data class Custom(override val code: Int) : SqliteVtabConstraintOperatorCode(code) {
        init {
            require(code >= FUNCTION.code) {
                "Custom constraint code must be greater than or equals to ${FUNCTION.code}"
            }
        }
    }
}