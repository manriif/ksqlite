@file:Suppress("SpellCheckingInspection")

package ksqlite.kapi.database

import ksqlite.kapi.buffer.OpaqueBuffer

/**
 * Exposes the database connection configuration API.
 *
 * [Database Connection Configuration Options](https://sqlite.org/c3ref/c_dbconfig_defensive.html)
 */
public interface DatabaseConnectionConfiguration {

    /**
     * Whether the foreign key constraints are enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isForeignKeyEnabled: Boolean

    /**
     * Whether triggers are enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var areTriggersEnabled: Boolean

    /**
     * Whether the `fts3_tokenizer()` function is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isFts3tokenizerEnabled: Boolean

    /**
     * Whether the sqlite3_load_extension() interface, independently of the `load_extension()` SQL
     * function, is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isLoadExtensionEnabled: Boolean

    /**
     * Whether the checkpoint performed by SQLite before closing the connection and deleting the WAL
     * file, after a dababase connection is closed or detached, is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isCheckpointOnCloseDisabled: Boolean

    /**
     * Whether the query planner stability guarantee is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isQueryPlannerStabilityGuaranteeEnabled: Boolean

    /**
     * Whether the output of operations performed by trigger programs is included in the
     * output of EXPLAIN QUERY PLAN commands.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isTriggerExplainQueryPlanEnabled: Boolean

    /**
     * Whether the "defensive" flag for a database connection is activated.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isDefensive: Boolean

    /**
     * Returns whether the "writable_schema" flag is activated.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isWritableSchema: Boolean

    /**
     * Whether the legacy behavior of the ALTER TABLE RENAME command such that it behaves as it did
     * prior to version 3.24.0 (2018-06-04) is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isLegacyAlterTableBehaviorEnabled: Boolean

    /**
     * Whether the legacy double-quoted string literal misfeature is enabled for DML statements
     * only, that is DELETE, INSERT, SELECT, and UPDATE statements.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isDoubleQuotedStringDmlEnabled: Boolean

    /**
     * Whether the llegacy double-quoted string literal misfeature is enabled for DDL statements,
     * such as CREATE TABLE and CREATE INDEX.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isDoubleQuotedStringDdlEnabled: Boolean

    /**
     * Whether views are enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var areViewsEnabled: Boolean

    /**
     * Whether the legacy file format flag is activated.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isLegacyFileFormatEnabled: Boolean

    /**
     * Whether SQLite should assume that database schemas are untainted by malicious content.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isTrustedSchema: Boolean

    /**
     * Whether the flag that enables collection of the sqlite3_stmt_scanstatus_v2() statistics is
     * activated.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isStatementScanStatusEnabled: Boolean

    /**
     * Whether the order in which tables and indexes are scanned are reversed so that the scans
     * start at the end and work toward the beginning rather than starting at the beginning and
     * working toward the end.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isReverseScanOrderEnabled: Boolean

    /**
     * Whether the ability of the ATTACH DATABASE SQL command to create a new database file if the
     * database filed named in the ATTACH command does not already exist is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isAttachCreateEnabled: Boolean

    /**
     * Whether the ability of the ATTACH DATABASE SQL command to open a database for writing is
     * enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var isAttachWriteEnabled: Boolean

    /**
     * Whether the ability to include comments in SQL text is enabled.
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public var areCommentsEnabled: Boolean

    /**
     * Number of significant digits that SQLite will attempt to preserve when converting floating
     * point numbers (IEEE 754 "doubles") into text.
     */
    public var floatingPointDigits: Int

    /**
     * Sets the name of the "main" database schema to [name].
     *
     * @throws ksqlite.kapi.SQLiteException if getting or setting the option fails.
     */
    public fun setMainDatabaseName(name: String)

    /**
     * Adjusts the configuration of the lookaside memory allocator within a database connection.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setLookasideConfig(
        buf: OpaqueBuffer?,
        sz: Int,
        cnt: Int
    )

    /**
     * Enables of disables the database reset mode.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setResetDatabaseEnabled(enabled: Boolean)
}