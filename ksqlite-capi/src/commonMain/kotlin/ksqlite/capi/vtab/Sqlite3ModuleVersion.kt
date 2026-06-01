package ksqlite.capi.vtab

/**
 * Defines the particular edition of the module table structure. Currently, iVersion is always
 * 4 or less, but in future releases of SQLite the module structure definition might be extended
 * with additional methods and in that case the maximum iVersion value will be increased.
 *
 * [Implementation](https://sqlite.org/vtab.html#implementation)
 */
public enum class Sqlite3ModuleVersion(internal val iVersion: Int) {

    /**
     * Initial version.
     */
    VERSION_1(1),

    /**
     * Adds support for xSavepoint, xRelease and xRollbackTo.
     */
    VERSION_2(2),

    /**
     * Adds support for xShadowName.
     *
     * Note that xShadowName is currently unsupported as SQLite doesn't supply enough information
     * in the callback to retrieve an associated Kotlin object. There is possible workarounds but
     * they're awkward.
     */
    VERSION_3(3),

    /**
     * Adds support for xIntegrity.
     */
    VERSION_4(4);
}