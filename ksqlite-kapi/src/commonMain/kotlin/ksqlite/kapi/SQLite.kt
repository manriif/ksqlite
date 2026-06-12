package ksqlite.kapi

import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3OpenFlag
import ksqlite.kapi.callbacks.AutoExtension
import ksqlite.kapi.impl.createSQLiteInstance

/**
 * SQLite instance is used to obtains [Connection].
 */
public interface SQLite : AutoCloseable {

    /**
     * Registers the [autoExtension] callback.
     * This method has no effect if the [autoExtension] is already registered.
     */
    public fun registerAutoExtension(autoExtension: AutoExtension)

    /**
     * Unregisters the [autoExtension] callback.
     * This method has no effect if the [autoExtension] is not registered.
     */
    public fun unregisterAutoExtension(autoExtension: AutoExtension)

    /**
     * Configures SQLite with supplied anytime [options].
     */
    public fun configure(options: List<Sqlite3ConfigOption.AnyTime>)

    /**
     * Opens a new database connection using `sqlite3_open_v2(), forwarding [flag] and [vfs].
     *
     * Once opened, [configure] is immediately invoked before the [Connection] is getting
     * returned.
     */
    public fun open(
        fileName: String,
        flag: Sqlite3OpenFlag.Db? = null,
        vfs: String? = null,
        initialize: (ConnectionInitializer.() -> Unit)? = null
    ): Connection

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
 * Initializes SQLite with given [options] and returns an [SQLite] instance used to initiate
 * connections.
 *
 * Indeed, [options] are set before `sqlite3_initialize()` is invoked.
 *
 * When the returned instance will no longer be needed, [SQLite.close] must be called and this
 * method can be called again with different [options].
 *
 * Only a single instance of [SQLite] exists at a time and an [IllegalStateException] is thrown if
 * a previously delivered instance of [SQLite] was not closed.
 */
public fun SQLite(options: Iterable<Sqlite3ConfigOption> = emptyList()): SQLite =
    createSQLiteInstance(options.toList())

/**
 * Initializes SQLite with given [options] and returns an [SQLite] instance used to initiate
 * connections.
 *
 * Indeed, [options] are set before `sqlite3_initialize()` is invoked.
 *
 * When the returned instance will no longer be needed, [SQLite.close] must be called and this
 * method can be called again with different [options].
 *
 * Only a single instance of [SQLite] exists at a time and an [IllegalStateException] is thrown if
 * a previously delivered instance of [SQLite] was not closed.
 */
public fun SQLite(vararg options: Sqlite3ConfigOption): SQLite =
    createSQLiteInstance(options.toList())

