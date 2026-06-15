package ksqlite.kapi.config

/**
 * SQLite logging interface.
 */
public fun interface Logger {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun log(
        errorCode: Int,
        message: String?
    )
}