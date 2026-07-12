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

package ksqlite.types

/**
 * These constants define various flags that can be passed into the "prepFlags" parameter of the
 * sqlite3_prepare_v3() and sqlite3_prepare16_v3() interfaces.
 *
 * [Prepare Flags](https://sqlite.org/c3ref/c_prepare_dont_log.html)
 */
public sealed class SqlitePrepareFlag(public open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : SqlitePrepareFlag(value)

    /**
     * The SQLITE_PREPARE_PERSISTENT flag is a hint to the query planner that the prepared statement
     * will be retained for a long time and probably reused many times. Without this flag,
     * sqlite3_prepare_v3() and sqlite3_prepare16_v3() assume that the prepared statement will be
     * used just once or at most a few times and then destroyed using sqlite3_finalize() relatively
     * soon. The current implementation acts on this hint by avoiding the use of lookaside memory so
     * as not to deplete the limited store of lookaside memory. Future versions of SQLite may act on
     * this hint differently.
     */
    public data object PERSISTENT : Constant(0x01)

    /**
     * The SQLITE_PREPARE_NORMALIZE flag is a no-op. This flag used to be required for any prepared
     * statement that wanted to use the sqlite3_normalized_sql() interface. However, the
     * sqlite3_normalized_sql() interface is now available to all prepared statements, regardless of
     * whether or not they use this flag.
     */
    public data object NORMALIZE : Constant(0x02)

    /**
     * The SQLITE_PREPARE_NO_VTAB flag causes the SQL compiler to return an error (error code
     * SQLITE_ERROR) if the statement uses any virtual tables.
     */
    public data object NO_VTAB : Constant(0x04)

    /**
     * The SQLITE_PREPARE_DONT_LOG flag prevents SQL compiler errors from being sent to the error
     * log defined by SQLITE_CONFIG_LOG. This can be used, for example, to do test compiles to see
     * if some SQL syntax is well-formed, without generating messages on the global error log when
     * it is not. If the test compile fails, the sqlite3_prepare_v3() call returns the same error
     * indications with or without this flag; it just omits the call to sqlite3_log() that logs the
     * error.
     */
    public data object DONT_LOG : Constant(0x10)

    /**
     * The SQLITE_PREPARE_FROM_DDL flag causes the SQL compiler to enforce security constraints that
     * would otherwise only be enforced when parsing the database schema. In other words, the
     * SQLITE_PREPARE_FROM_DDL flag causes the SQL compiler to treat the SQL statement being
     * prepared as if it had come from an attacker. When SQLITE_PREPARE_FROM_DDL is used and
     * SQLITE_DBCONFIG_TRUSTED_SCHEMA is off, SQL functions may only be called if they are tagged
     * with SQLITE_INNOCUOUS and virtual tables may only be used if they are tagged with
     * SQLITE_VTAB_INNOCUOUS. Best practice is to use the SQLITE_PREPARE_FROM_DDL option when
     * preparing any SQL that is derived from parts of the database schema. In particular, virtual
     * table implementations that run SQL statements that are derived from arguments to their
     * CREATE VIRTUAL TABLE statement should always use sqlite3_prepare_v3() and set the
     * SQLITE_PREPARE_FROM_DDL flag to prevent bypass of the SQLITE_DBCONFIG_TRUSTED_SCHEMA security
     * checks.
     */
    public data object FROM_DDL : Constant(0x20)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the prepare API functions.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqlitePrepareFlag(value) {

        override fun contains(flag: SqlitePrepareFlag): Boolean =
            (value and flag.value) == flag.value
    }

    /**
     * Returns an [SqlitePrepareFlag] which is ORed with [flag].
     */
    public infix fun or(flag: SqlitePrepareFlag): SqlitePrepareFlag =
        Mask(value or flag.value)

    /**
     * Returns an [SqlitePrepareFlag] which is ANDed with [flag].
     */
    public infix fun and(flag: SqlitePrepareFlag): SqlitePrepareFlag =
        Mask(value and flag.value)

    /**
     * Returns an [SqlitePrepareFlag] which has [flag] removed.
     */
    public infix fun without(flag: SqlitePrepareFlag): SqlitePrepareFlag =
        Mask(value and flag.value.inv())

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: SqlitePrepareFlag): Boolean =
        flag == this || flag.value == value
}