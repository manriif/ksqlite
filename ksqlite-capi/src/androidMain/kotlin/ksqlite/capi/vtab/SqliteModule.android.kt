@file:Suppress("ClassName", "ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")

package ksqlite.capi.vtab

import ksqlite.capi.memory.AllocatableStruct

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    module: s3_module
) : AllocatableStruct(module),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VtabModuleCallbacks<AppData, *, *>
    ) : this(
        callbacks,
        s3_module(
            callbacks = VtabModuleHandler,
            callbackMask = callbacks.computeCallbackMask(),
            eponymous = callbacks.moduleKind == SqliteModuleKind.Eponymous
        ).apply {
            iVersion = version
        }
    )
}

/**
 * Computes and returns a mask of optional callbacks that are enabled.
 */
private fun VtabModuleCallbacks<*, *, *>.computeCallbackMask(): Int {
    var mask = 0

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

    return mask
}