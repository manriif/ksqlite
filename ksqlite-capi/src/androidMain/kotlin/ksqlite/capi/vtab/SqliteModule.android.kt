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
import ksqlite.capi.memory.PointerOwner.Application

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    module: s3_module
) : ClosableStruct(module, Application),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VtabModuleCallbacks<AppData, *, *>
    ) : this(
        callbacks,
        s3_module(
            callbacks = VtabModuleHandler,
            eponymous = callbacks.moduleKind == SqliteModuleKind.Eponymous,
            optionalCallbacks = buildSet {
                callbacks.run {
                    create?.let { add(XCREATE) }
                    update?.let { add(XUPDATE) }
                    begin?.let { add(XBEGIN) }
                    sync?.let { add(XSYNC) }
                    commit?.let { add(XCOMMIT) }
                    rollback?.let { add(XROLLBACK) }
                    findFunction?.let { add(XFINDFUNCTION) }
                    rename?.let { add(XRENAME) }
                    savepoint?.let { add(XSAVEPOINT) }
                    release?.let { add(XRELEASE) }
                    rollbackTo?.let { add(XROLLBACKTO) }
                    integrity?.let { add(XINTEGRITY) }
                }
            }
        ).apply {
            iVersion = version
        }
    )
}