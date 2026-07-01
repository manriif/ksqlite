package ksqlite.capi.vtab.callbacks

/**
 * Some virtual table implementations (ex: FTS3, FTS5, and RTREE) make use of real (non-virtual)
 * database tables to store content. For example, when content is inserted into the FTS3 virtual
 * table, the data is ultimately stored in real tables named "%_content", "%_segdir", "%_segments",
 * "%_stat", and "%_docsize" where "%" is the name of the original virtual table. These auxiliary
 * real tables that store content for a virtual table are called "shadow tables". See (1), (2), and
 * (3) for additional information.
 *
 * The xShadowName method exists to allow SQLite to determine whether a certain real table is in
 * fact a shadow table for a virtual table.
 *
 * [The xShadowName Method](https://sqlite.org/vtab.html#the_xshadowname_method)
 */
public fun interface SqliteVtabShadowNameCallback {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xshadowname_method).
     */
    public fun apply(name: String): Int
}