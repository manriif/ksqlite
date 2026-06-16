package ksqlite.kapi

import ksqlite.kapi.config.AnyTimeConfiguration
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.database.AutoExtension
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.types.SqliteOpenFlag

/**
 * [SQLite](https://sqlite.org/docs.html) entry point.
 */
public interface SQLite : AutoCloseable {

    /**
     * Configuration exposing options that can be accessed at anytime.
     */
    public val config: AnyTimeConfiguration

    /**
     * Opens a new database connection.
     *
     * @throws SQLiteException if an error happens while opening the connection or if an
     * [AutoExtension] fails.
     */
    public fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db = SqliteOpenFlag.READONLY,
        vfs: String? = null
    ): DatabaseConnection

    /**
     * Registers the [autoExtension] callback.
     * This method has no effect if the [autoExtension] is already registered.
     */
    public fun addAutoExtension(autoExtension: AutoExtension)

    /**
     * Unregisters the [autoExtension] callback.
     * This method has no effect if the [autoExtension] is not registered.
     */
    public fun removeAutoExtension(autoExtension: AutoExtension)

    /**
     * Invokes `sqlite3_shutdown()` and resets global SQLite state.
     *
     * It is recommended to terminate any active statement, transaction and opened database
     * connection first before closing `this` [SQLite] instance.
     *
     * @throws SQLiteException if error happens while shutting down SQLite.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides access to SQLite APIs that do not require SQLite initialization.
     */
    public companion object {

        /**
         * Returns the options that were defined at compile-time.
         * The `SQLITE_` prefix is omitted for each option.
         */
        public val compileOptions: List<String>
            get() = SqliteCompileOptions

        /**
         * Returns `true` if `this` seems to form a complete SQL statement. If additional input is
         * needed before sending tbe text into SQLite for parsing, then `false` is returned.
         *
         * @throws SQLiteException if a memory allocation fails.
         */
        public fun String.isCompleteSqlStatement(): Boolean = sqliteIsComplete(this)
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes SQLite and returns an [SQLite] instance used to initiate connections.
 * SQLite options can be configured by supplying a value to [configure].
 *
 * When the returned instance will no longer be needed, [SQLite.close] must be called and this
 * method can be called again.
 *
 * Only a single instance of [SQLite] exists at a time and an [IllegalStateException] is thrown if
 * a previously returned instance of [SQLite] was not closed.
 *
 * @throws SQLiteException if an operation fails while creating and configuring [SQLite].
 */
public fun SQLite(configure: (ConfigurationScope.() -> Unit)? = null): SQLite =
    sqliteInitialize(configure)