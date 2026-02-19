@file:Suppress("SpellCheckingInspection", "ClassName")

package ksqlite.types

/**
 * These constants are the available integer configuration options that can be passed as the second
 * parameter to the sqlite3_db_config() interface.
 *
 * [Database Connection Configuration Options](https://sqlite.org/c3ref/c_dbconfig_defensive.html)
 */
public sealed class Sqlite3DbConfigOption(internal val id: Int) {

    /**
     * This option is used to change the name of the "main" database schema. This option does not
     * follow the usual SQLITE_DBCONFIG argument format. This option takes exactly one additional
     * argument so that the sqlite3_db_config() call has a total of three parameters. The extra
     * argument must be a pointer to a constant UTF8 string which will become the new schema name
     * in place of "main". SQLite does not make a copy of the new main schema name string, so the
     * application must ensure that the argument passed into SQLITE_DBCONFIG MAINDBNAME is unchanged
     * until after the database connection closes.
     */
    public class MAINDBNAME(internal val name: String) : Sqlite3DbConfigOption(1000)

    /**
     * The SQLITE_DBCONFIG_LOOKASIDE option is used to adjust the configuration of the lookaside
     * memory allocator within a database connection. The arguments to the SQLITE_DBCONFIG_LOOKASIDE
     * option are not in the usual format. The SQLITE_DBCONFIG_LOOKASIDE option takes three
     * arguments, not two, so that a call to sqlite3_db_config() that uses SQLITE_DBCONFIG_LOOKASIDE
     * should have a total of five parameters.
     *
     * 1. The first argument ("buf") is a pointer to a memory buffer to use for lookaside memory.
     * The first argument may be NULL in which case SQLite will allocate the lookaside buffer
     * itself using sqlite3_malloc().
     *
     * 2. The second argument ("sz") is the size of each lookaside buffer slot. Lookaside is
     * disabled if "sz" is less than 8. The "sz" argument should be a multiple of 8 less than 65536.
     * If "sz" does not meet this constraint, it is reduced in size until it does.
     *
     * 3. The third argument ("cnt") is the number of slots. Lookaside is disabled if "cnt"is less
     * than 1. The "cnt" value will be reduced, if necessary, so that the product of "sz" and "cnt"
     * does not exceed 2,147,418,112. The "cnt" parameter is usually chosen so that the product of
     * "sz" and "cnt" is less than 1,000,000.
     *
     * If the "buf" argument is not NULL, then it must point to a memory buffer with a size that is
     * greater than or equal to the product of "sz" and "cnt". The buffer must be aligned to an
     * 8-byte boundary. The lookaside memory configuration for a database connection can only be
     * changed when that connection is not currently using lookaside memory, or in other words when
     * the value returned by SQLITE_DBSTATUS_LOOKASIDE_USED is zero. Any attempt to change the
     * lookaside memory configuration when lookaside memory is in use leaves the configuration
     * unchanged and returns SQLITE_BUSY. If the "buf" argument is NULL and an attempt to allocate
     * memory based on "sz" and "cnt" fails, then lookaside is silently disabled.
     *
     * The SQLITE_CONFIG_LOOKASIDE configuration option can be used to set the default lookaside
     * configuration at initialization. The -DSQLITE_DEFAULT_LOOKASIDE option can be used to set the
     * default lookaside configuration at compile-time. Typical values for lookaside are 1200 for
     * "sz" and 40 to 100 for "cnt".
     */
    public class LOOKASIDE(
        internal val buf: pointer?,
        internal val sz: Int,
        internal val cnt: Int
    ) : Sqlite3DbConfigOption(1001)

    /**
     * This option is used to enable or disable the enforcement of foreign key constraints. This is
     * the same setting that is enabled or disabled by the PRAGMA foreign_keys statement. The first
     * argument is an integer which is 0 to disable FK enforcement, positive to enable FK
     * enforcement or negative to leave FK enforcement unchanged. The second parameter is a pointer
     * to an integer into which is written 0 or 1 to indicate whether FK enforcement is off or on
     * following this call. The second parameter may be a NULL pointer, in which case the FK
     * enforcement setting is not reported back.
     */
    public class ENABLE_FKEY(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1002)

    /**
     * This option is used to enable or disable triggers. There should be two additional arguments.
     * The first argument is an integer which is 0 to disable triggers, positive to enable triggers
     * or negative to leave the setting unchanged. The second parameter is a pointer to an integer
     * into which is written 0 or 1 to indicate whether triggers are disabled or enabled following
     * this call. The second parameter may be a NULL pointer, in which case the trigger setting is
     * not reported back.
     */
    public class ENABLE_TRIGGER(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1003)

    /**
     * This option is used to enable or disable using the fts3_tokenizer() function - part of the
     * FTS3 full-text search engine extension - without using bound parameters as the parameters.
     * Doing so is disabled by default. There must be two additional arguments. The first argument
     * is an integer. If it is passed 0, then using fts3_tokenizer() without bound parameters is
     * disabled. If it is passed a positive value, then calling fts3_tokenizer without bound
     * parameters is enabled. If it is passed a negative value, this setting is not modified -
     * this can be used to query for the current setting. The second parameter is a pointer to an
     * integer into which is written 0 or 1 to indicate the current value of this setting (after it
     * is modified, if applicable). The second parameter may be a NULL pointer, in which case the
     * value of the setting is not reported back. Refer to FTS3 documentation for further details.
     */
    public class ENABLE_FTS3_TOKENIZER(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1004)

    /**
     * This option is used to enable or disable the sqlite3_load_extension() interface independently
     * of the load_extension() SQL function. The sqlite3_enable_load_extension() API enables or
     * disables both the C-API sqlite3_load_extension() and the SQL function load_extension().
     * There must be two additional arguments. When the first argument to this interface is 1, then
     * only the C-API is enabled and the SQL function remains disabled. If the first argument to
     * this interface is 0, then both the C-API and the SQL function are disabled. If the first
     * argument is -1, then no changes are made to the state of either the C-API or the SQL
     * function. The second parameter is a pointer to an integer into which is written 0 or 1 to
     * indicate whether sqlite3_load_extension() interface is disabled or enabled following this
     * call. The second parameter may be a NULL pointer, in which case the new setting is not
     * reported back.
     */
    public class ENABLE_LOAD_EXTENSION(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1005)

    /**
     * Usually, when a database in WAL mode is closed or detached from a database handle, SQLite
     * checks if there are other connections to the same database, and if there are no other
     * database connection (if the connection being closed is the last open connection to the
     * database), then SQLite performs a checkpoint before closing the connection and deletes the
     * WAL file. The SQLITE_DBCONFIG_NO_CKPT_ON_CLOSE option can be used to override that behavior.
     * The first argument passed to this operation (the third parameter to sqlite3_db_config()) is
     * an integer which is positive to disable checkpoints-on-close, or zero (the default) to enable
     * them, and negative to leave the setting unchanged. The second argument (the fourth parameter)
     * is a pointer to an integer into which is written 0 or 1 to indicate whether
     * checkpoints-on-close have been disabled - 0 if they are not disabled, 1 if they are.
     */
    public class NO_CKPT_ON_CLOSE(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1006)

    /**
     * The SQLITE_DBCONFIG_ENABLE_QPSG option activates or deactivates the query planner stability
     * guarantee (QPSG). When the QPSG is active, a single SQL query statement will always use the
     * same algorithm regardless of values of bound parameters. The QPSG disables some query
     * optimizations that look at the values of bound parameters, which can make some queries
     * slower. But the QPSG has the advantage of more predictable behavior. With the QPSG active,
     * SQLite will always use the same query plan in the field as was used during testing in the
     * lab. The first argument to this setting is an integer which is 0 to disable the QPSG,
     * positive to enable QPSG, or negative to leave the setting unchanged. The second parameter is
     * a pointer to an integer into which is written 0 or 1 to indicate whether the QPSG is disabled
     * or enabled following this call.
     */
    public class ENABLE_QPSG(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1007)

    /**
     * By default, the output of EXPLAIN QUERY PLAN commands does not include output for any 
     * operations performed by trigger programs. This option is used to set or clear (the default) 
     * a flag that governs this behavior. The first parameter passed to this operation is an integer
     * - positive to enable output for trigger programs, or zero to disable it, or negative to leave
     * the setting unchanged. The second parameter is a pointer to an integer into which is written
     * 0 or 1 to indicate whether output-for-triggers has been disabled - 0 if it is not disabled, 
     * 1 if it is.
     */
    public class TRIGGER_EQP(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1008)

    /**
     * Set the SQLITE_DBCONFIG_RESET_DATABASE flag and then run VACUUM in order to reset a database
     * back to an empty database with no schema and no content. The following process works even for
     * a badly corrupted database file:
     *
     * 1. If the database connection is newly opened, make sure it has read the database schema by
     * preparing then discarding some query against the database, or calling
     * sqlite3_table_column_metadata(), ignoring any errors. This step is only necessary if the
     * application desires to keep the database in WAL mode after the reset if it was in WAL mode
     * before the reset.
     *
     * 2. ```sqlite3_db_config(db, SQLITE_DBCONFIG_RESET_DATABASE, 1, 0);```
     *
     * 3. ```sqlite3_exec(db, "VACUUM", 0, 0, 0);```
     *
     * 4. ```sqlite3_db_config(db, SQLITE_DBCONFIG_RESET_DATABASE, 0, 0);```
     *
     * Because resetting a database is destructive and irreversible, the process requires the use of
     * this obscure API and multiple steps to help ensure that it does not happen by accident.
     * Because this feature must be capable of resetting corrupt databases, and shutting down
     * virtual tables may require access to that corrupt storage, the library must abandon any
     * installed virtual tables without calling their xDestroy() methods.
     */
    public class RESET_DATABASE(
        internal val value: Int,
        internal val state: Sqlite3IntParam?
    ) : Sqlite3DbConfigOption(1009)

    /**
     * The SQLITE_DBCONFIG_DEFENSIVE option activates or deactivates the "defensive" flag for a
     * database connection. When the defensive flag is enabled, language features that allow
     * ordinary SQL to deliberately corrupt the database file are disabled. The disabled features
     * include but are not limited to the following:
     *
     * - The PRAGMA writable_schema=ON statement.
     * - The PRAGMA journal_mode=OFF statement.
     * - The PRAGMA schema_version=N statement.
     * - Writes to the sqlite_dbpage virtual table.
     * - Direct writes to shadow tables.
     */
    public class DEFENSIVE(
        internal val value: Int,
        internal val state: Sqlite3IntParam?
    ) : Sqlite3DbConfigOption(1010)

    /**
     * The SQLITE_DBCONFIG_WRITABLE_SCHEMA option activates or deactivates the "writable_schema"
     * flag. This has the same effect and is logically equivalent to setting PRAGMA
     * writable_schema=ON or PRAGMA writable_schema=OFF. The first argument to this setting is an
     * integer which is 0 to disable the writable_schema, positive to enable writable_schema, or
     * negative to leave the setting unchanged. The second parameter is a pointer to an integer into
     * which is written 0 or 1 to indicate whether the writable_schema is enabled or disabled
     * following this call.
     */
    public class WRITABLE_SCHEMA(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1011)

    /**
     * The SQLITE_DBCONFIG_LEGACY_ALTER_TABLE option activates or deactivates the legacy behavior of
     * the ALTER TABLE RENAME command such that it behaves as it did prior to version 3.24.0
     * (2018-06-04). See the "Compatibility Notice" on the ALTER TABLE RENAME documentation for
     * additional information. This feature can also be turned on and off using the PRAGMA
     * legacy_alter_table statement.
     */
    public class LEGACY_ALTER_TABLE(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1012)

    /**
     * The SQLITE_DBCONFIG_DQS_DML option activates or deactivates the legacy double-quoted string
     * literal misfeature for DML statements only, that is DELETE, INSERT, SELECT, and UPDATE
     * statements. The default value of this setting is determined by the -DSQLITE_DQS compile-time
     * option.
     */
    public class DQS_DML(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1013)

    /**
     * The SQLITE_DBCONFIG_DQS option activates or deactivates the legacy double-quoted string
     * literal misfeature for DDL statements, such as CREATE TABLE and CREATE INDEX. The default
     * value of this setting is determined by the -DSQLITE_DQS compile-time option.
     */
    public class DQS_DDL(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1014)

    /**
     * This option is used to enable or disable views. There must be two additional arguments. The
     * first argument is an integer which is 0 to disable views, positive to enable views or
     * negative to leave the setting unchanged. The second parameter is a pointer to an integer into
     * which is written 0 or 1 to indicate whether views are disabled or enabled following this
     * call. The second parameter may be a NULL pointer, in which case the view setting is not
     * reported back.
     * Originally this option disabled all views. However, since SQLite version 3.35.0, TEMP views
     * are still allowed even if this option is off. So, in other words, this option now only
     * disables views in the main database schema or in the schemas of ATTACH-ed databases.
     */
    public class ENABLE_VIEW(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1015)

    /**
     * The SQLITE_DBCONFIG_LEGACY_FILE_FORMAT option activates or deactivates the legacy file format
     * flag. When activated, this flag causes all newly created database files to have a schema
     * format version number (the 4-byte integer found at offset 44 into the database header) of 1.
     * This in turn means that the resulting database file will be readable and writable by any
     * SQLite version back to 3.0.0 (2004-06-18). Without this setting, newly created databases are
     * generally not understandable by SQLite versions prior to 3.3.0 (2006-01-11). As these words
     * are written, there is now scarcely any need to generate database files that are compatible
     * all the way back to version 3.0.0, and so this setting is of little practical use, but is
     * provided so that SQLite can continue to claim the ability to generate new database files that
     * are compatible with version 3.0.0.
     *
     * Note that when the SQLITE_DBCONFIG_LEGACY_FILE_FORMAT setting is on, the VACUUM command will
     * fail with an obscure error when attempting to process a table with generated columns and a
     * descending index. This is not considered a bug since SQLite versions 3.3.0 and earlier do not
     * support either generated columns or descending indexes.
     */
    public class LEGACY_FILE_FORMAT(
        internal val value: Int,
        internal val state: Sqlite3IntParam?
    ) : Sqlite3DbConfigOption(1016)

    /**
     * The SQLITE_DBCONFIG_TRUSTED_SCHEMA option tells SQLite to assume that database schemas are
     * untainted by malicious content. When the SQLITE_DBCONFIG_TRUSTED_SCHEMA option is disabled,
     * SQLite takes additional defensive steps to protect the application from harm including:
     *
     * - Prohibit the use of SQL functions inside triggers, views, CHECK constraints, DEFAULT clauses,
     * expression indexes, partial indexes, or generated columns unless those functions are tagged
     * with SQLITE_INNOCUOUS.
     *
     * - Prohibit the use of virtual tables inside of triggers or views unless those virtual tables
     * are tagged with SQLITE_VTAB_INNOCUOUS.
     *
     * This setting defaults to "on" for legacy compatibility, however all applications are advised
     * to turn it off if possible. This setting can also be controlled using the PRAGMA
     * trusted_schema statement.
     */
    public class TRUSTED_SCHEMA(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1017)

    /**
     * The SQLITE_DBCONFIG_STMT_SCANSTATUS option is only useful in SQLITE_ENABLE_STMT_SCANSTATUS
     * builds. In this case, it sets or clears a flag that enables collection of the
     * sqlite3_stmt_scanstatus_v2() statistics. For statistics to be collected, the flag must be set
     * on the database handle both when the SQL statement is prepared and when it is stepped. The
     * flag is set (collection of statistics is enabled) by default.
     *
     * This option takes two arguments: an integer and a pointer to an integer. The first argument
     * is 1, 0, or -1 to enable, disable, or leave unchanged the statement scanstatus option. If the
     * second argument is not NULL, then the value of the statement scanstatus setting after
     * processing the first argument is written into the integer that the second argument points to.
     */
    public class STMT_SCANSTATUS(
        internal val value: Int,
        internal val state: Sqlite3IntParam?
    ) : Sqlite3DbConfigOption(1018)

    /**
     * The SQLITE_DBCONFIG_REVERSE_SCANORDER option changes the default order in which tables and
     * indexes are scanned so that the scans start at the end and work toward the beginning rather
     * than starting at the beginning and working toward the end. Setting
     *
     * SQLITE_DBCONFIG_REVERSE_SCANORDER is the same as setting PRAGMA reverse_unordered_selects.
     * This option takes two arguments which are an integer and a pointer to an integer. The first
     * argument is 1, 0, or -1 to enable, disable, or leave unchanged the reverse scan order flag,
     * respectively. If the second argument is not NULL, then 0 or 1 is written into the integer
     * that the second argument points to depending on if the reverse scan order flag is set after
     * processing the first argument.
     */
    public class REVERSE_SCANORDER(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1019)

    /**
     * The SQLITE_DBCONFIG_ENABLE_ATTACH_CREATE option enables or disables the ability of the ATTACH
     * DATABASE SQL command to create a new database file if the database filed named in the ATTACH
     * command does not already exist. This ability of ATTACH to create a new database is enabled by
     * default. Applications can disable or reenable the ability for ATTACH to create new database
     * files using this DBCONFIG option.
     *
     * This option takes two arguments which are an integer and a pointer to an integer. The first
     * argument is 1, 0, or -1 to enable, disable, or leave unchanged the attach-create flag,
     * respectively. If the second argument is not NULL, then 0 or 1 is written into the integer
     * that the second argument points to depending on if the attach-create flag is set after
     * processing the first argument.
     */
    public class ENABLE_ATTACH_CREATE(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1020)

    /**
     * The SQLITE_DBCONFIG_ENABLE_ATTACH_WRITE option enables or disables the ability of the ATTACH
     * DATABASE SQL command to open a database for writing. This capability is enabled by default.
     * Applications can disable or reenable this capability using the current DBCONFIG option. If
     * this capability is disabled, the ATTACH command will still work, but the database will be
     * opened read-only. If this option is disabled, then the ability to create a new database
     * using ATTACH is also disabled, regardless of the value of the
     * SQLITE_DBCONFIG_ENABLE_ATTACH_CREATE option.
     *
     * This option takes two arguments which are an integer and a pointer to an integer. The first
     * argument is 1, 0, or -1 to enable, disable, or leave unchanged the ability to ATTACH another
     * database for writing, respectively. If the second argument is not NULL, then 0 or 1 is
     * written into the integer to which the second argument points, depending on whether the
     * ability to ATTACH a read/write database is enabled or disabled after processing the first
     * argument.
     */
    public class ENABLE_ATTACH_WRITE(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1021)

    /**
     * The SQLITE_DBCONFIG_ENABLE_COMMENTS option enables or disables the ability to include
     * comments in SQL text. Comments are enabled by default. An application can disable or reenable
     * comments in SQL text using this DBCONFIG option.
     *
     * This option takes two arguments which are an integer and a pointer to an integer. The first
     * argument is 1, 0, or -1 to enable, disable, or leave unchanged the ability to use comments in
     * SQL text, respectively. If the second argument is not NULL, then 0 or 1 is written into the
     * integer that the second argument points to depending on if comments are allowed in SQL text
     * after processing the first argument.
     */
    public class ENABLE_COMMENTS(
        internal val value: Int,
        internal val state: Sqlite3BooleanParam?
    ) : Sqlite3DbConfigOption(1022)
}