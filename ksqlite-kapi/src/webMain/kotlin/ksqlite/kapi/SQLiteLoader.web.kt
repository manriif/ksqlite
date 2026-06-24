package ksqlite.kapi

import ksqlite.capi.SqliteLoaderConfig
import ksqlite.capi.isSqliteLoaded
import ksqlite.capi.sqliteLoad
import ksqlite.kapi.SQLiteLoader.Config

/**
 * Loader for the SQLite library.
 */
public actual interface SQLiteLoader {

    /**
     * Configuration for the SQLite module loader.
     */
    public interface Config : SqliteLoaderConfig

    /**
     * Loads the SQLite library without initializing it.
     * The loading process can be customized within [configure].
     *
     * This function must be called before any of the [SQLite] members.
     * It is safe to call this function more than once.
     */
    public suspend fun load(configure: (Config.() -> Unit)? = null)
}

internal object SQLiteLoaderImpl : SQLiteLoader {

    override suspend fun load(configure: (Config.() -> Unit)?) {
        if (!isSqliteLoaded) {
            if (configure == null) {
                sqliteLoad(null)
            } else {
                sqliteLoad {
                    configure(object : Config, SqliteLoaderConfig by this {})
                }
            }
        }
    }
}

public actual fun sqliteLoader(): SQLiteLoader = SQLiteLoaderImpl