package ksqlite.kapi

import ksqlite.kapi.config.AnyTimeConfigurationScope
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.connection.Connection
import ksqlite.types.SqliteOpenFlag

/**
 * SQLite instance is used to obtains [ksqlite.kapi.connection.Connection].
 */
public interface SQLite : AutoCloseable {

    /**
     * Configures SQLite anytime options.
     */
    public fun configure(action: AnyTimeConfigurationScope.() -> Unit)

    /**
     * Opens a new database connection.
     */
    public fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db = SqliteOpenFlag.READONLY,
        vfs: String? = null
    ): Connection

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
     */
    override fun close()
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
    createSQLiteInstance(configure)