package ksqlite.capi.vtab

/**
 * Kind of Virtual Table module.
 */
internal enum class SqliteModuleKind {

    /**
     * xCreate is not null but distinct from xConnect.
     */
    Regular,

    /**
     * xCreate is referentially equals to xConnect.
     */
    Eponymous,

    /**
     * xCreate is null.
     */
    EponymousOnly
}