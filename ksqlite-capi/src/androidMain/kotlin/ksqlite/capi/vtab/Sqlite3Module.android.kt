@file:Suppress("ClassName", "ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VTabModuleCallbacks<AppData, *, *>,
    module: s3_module
) :
    Struct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VTabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, s3_module().apply {
        iVersion = version
    })

    /**
     * Computes and returns a mask of optional callbacks that are enabled.
     */
    internal fun computeCallbackMask(): Int {
        var mask = 0

        with(callbacks) {
            create?.let { mask = 1.shl(s3_module.STRUCT_MEMBER_INDEX_XCREATE) }
            update?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XUPDATE) }
            begin?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XBEGIN) }
            sync?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XSYNC) }
            commit?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XCOMMIT) }
            rollback?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XROLLBACK) }
            findFunction?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XFINDFUNCTION) }
            rename?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XRENAME) }
            savepoint?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XSAVEPOINT) }
            release?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XRELEASE) }
            rollbackTo?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XROLLBACKTO) }
            integrity?.let { mask = mask or 1.shl(s3_module.STRUCT_MEMBER_INDEX_XINTEGRITY) }
        }

        return mask
    }

    actual override fun close(): Unit = free()
}