/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ClassName", "ACTUAL_IGNORABILITY_NOT_MATCH_EXPECT")

package ksqlite.capi.vtab

import ksqlite.capi.memory.ClosableStruct

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    module: s3_module
) : ClosableStruct(module),
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