@file:Suppress("ClassName", "ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.nativeVTabModuleInit

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>,
    module: s3_module) :
    Struct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VTabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, s3_module().apply {
        iVersion = version

        var mask = 0

        with(callbacks) {
            create?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XCREATE) }
            update?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XUPDATE) }
            begin?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XBEGIN) }
            sync?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XSYNC) }
            commit?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XCOMMIT) }
            rollback?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XROLLBACK) }
            findFunction?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XFINDFUNCTION) }
            rename?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XRENAME) }
            savepoint?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XSAVEPOINT) }
            release?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XRELEASE) }
            rollbackTo?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XROLLBACKTO) }
            integrity?.let { mask = mask or 1.shl(s3_module.LAYOUT_INDEX_XINTEGRITY) }
        }

        nativeVTabModuleInit(
            module = pointer,
            methodMask = mask.shr(1),
            eponymous = callbacks.moduleKind == Sqlite3ModuleKind.Eponymous,
            callbacks = VTabModuleHandler
        )
    })

    actual override fun close(): Unit = free()
}