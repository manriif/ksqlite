package ksqlite.capi.vtab

import ksqlite.capi.types.sqlite3

/**
 * Definition of [sqlite3_module].
 */
public interface Sqlite3Module<AppData, VTab : Sqlite3VTab, VTabCursor : Sqlite3VTabCursor> {

    /**
     * Defines the particular edition of the module table structure. Currently, iVersion is always
     * 4 or less, but in future releases of SQLite the module structure definition might be extended
     * with additional methods and in that case the maximum iVersion value will be increased.
     */
    public val iVersion: Sqlite3ModuleVersion

    /**
     * The xCreate method is called to create a new instance of a virtual table in response to a
     * CREATE VIRTUAL TABLE statement. If the xCreate method is the same pointer as the xConnect
     * method, then the virtual table is an eponymous virtual table. If the xCreate method is
     * omitted (if it is a NULL pointer) then the virtual table is an eponymous-only virtual table.
     */
    public fun CreateScope<VTab>.create(
        db: sqlite3,
        appData: AppData,
        argv: Array<String>
    ): CreateResult<VTab>

    /**
     * The xConnect method is very similar to xCreate. It has the same parameters and constructs a
     * new sqlite3_vtab structure just like xCreate. And it must also call sqlite3_declare_vtab()
     * like xCreate. It should also make all of the same sqlite3_vtab_config() calls as xCreate.
     *
     * The difference is that xConnect is called to establish a new connection to an existing
     * virtual table whereas xCreate is called to create a new virtual table from scratch.
     */
    public fun ConnectScope<VTab>.connect(
        db: sqlite3,
        appData: AppData,
        argv: Array<String>
    ): ConnectResult<VTab>

    /**
     * SQLite uses the xBestIndex method of a virtual table module to determine the best way to
     * access the virtual table.
     */
    public fun bestIndex(vTab: VTab, info: sqlite3_index_info): Int

    public fun disconnect(vTab: VTab)


    int (*xDisconnect)(sqlite3_vtab *pVTab);
    int (*xDestroy)(sqlite3_vtab *pVTab);
    int (*xOpen)(sqlite3_vtab *pVTab, sqlite3_vtab_cursor **ppCursor);
    int (*xClose)(sqlite3_vtab_cursor*);
    int (*xFilter)(sqlite3_vtab_cursor*, int idxNum, const char *idxStr,
    int argc, sqlite3_value **argv);
    int (*xNext)(sqlite3_vtab_cursor*);
    int (*xEof)(sqlite3_vtab_cursor*);
    int (*xColumn)(sqlite3_vtab_cursor*, sqlite3_context*, int);
    int (*xRowid)(sqlite3_vtab_cursor*, sqlite3_int64 *pRowid);
    int (*xUpdate)(sqlite3_vtab *, int, sqlite3_value **, sqlite3_int64 *);
    int (*xBegin)(sqlite3_vtab *pVTab);
    int (*xSync)(sqlite3_vtab *pVTab);
    int (*xCommit)(sqlite3_vtab *pVTab);
    int (*xRollback)(sqlite3_vtab *pVTab);
    int (*xFindFunction)(sqlite3_vtab *pVtab, int nArg, const char *zName,
    void (**pxFunc)(sqlite3_context*,int,sqlite3_value**),
    void **ppArg);
    int (*xRename)(sqlite3_vtab *pVtab, const char *zNew);
    /* The methods above are in version 1 of the sqlite_module object. Those
    ** below are for version 2 and greater. */
    int (*xSavepoint)(sqlite3_vtab *pVTab, int);
    int (*xRelease)(sqlite3_vtab *pVTab, int);
    int (*xRollbackTo)(sqlite3_vtab *pVTab, int);
    /* The methods above are in versions 1 and 2 of the sqlite_module object.
    ** Those below are for version 3 and greater. */
    int (*xShadowName)(const char*);
    /* The methods above are in versions 1 through 3 of the sqlite_module object.
    ** Those below are for version 4 and greater. */
    int (*xIntegrity)(sqlite3_vtab *pVTab, const char *zSchema,
    const char *zTabName, int mFlags, char **pzErr);

    ///////////////////////////////////////////////////////////////////////////
    // Scopes & Results
    ///////////////////////////////////////////////////////////////////////////

    public sealed interface ConnectResult<out VTab : Sqlite3VTab>

    public sealed interface ConnectScope<VTab : Sqlite3VTab> {

        public fun success(vTab: VTab): CreateResult<VTab>

        public fun failure(error: String): CreateResult<Nothing>
    }

    public sealed interface CreateResult<out VTab : Sqlite3VTab>

    public sealed interface CreateScope<VTab : Sqlite3VTab> : ConnectScope<VTab> {

        public fun eponymous(): CreateResult<Nothing>

        public fun eponymousOnly(): CreateResult<Nothing>
    }
}