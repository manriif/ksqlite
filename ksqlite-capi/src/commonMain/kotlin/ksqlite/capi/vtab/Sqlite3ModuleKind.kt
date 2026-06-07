package ksqlite.capi.vtab

/**
 * Kind of Virtual Table module.
 */
internal enum class Sqlite3ModuleKind {

    /**
     * xCreate is not null but distinct from xConnect.
     */
    Ordinal,

    /**
     * xCreate is referentially equals to xConnect.
     */
    Eponymous,

    /**
     * xCreate is null.
     */
    EponymousOnly
}