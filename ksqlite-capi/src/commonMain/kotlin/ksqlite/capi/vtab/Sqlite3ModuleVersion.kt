package ksqlite.capi.vtab

/**
 * Available versions for [Sqlite3Module.iVersion].
 */
public enum class Sqlite3ModuleVersion(internal val iVersion: Int) {

    /**
     * First version.
     */
    VERSION_1(1),

    /**
     * Adds support for [Sqlite3Module.savepoint], [Sqlite3Module.release] and
     * [Sqlite3Module.rollbackTo].
     */
    VERSION_2(2),

    /**
     * Adds support for [Sqlite3Module.shadowName].
     */
    VERSION_3(3),

    /**
     * Adds support for [Sqlite3Module.integrity].
     */
    VERSION_4(4);
}