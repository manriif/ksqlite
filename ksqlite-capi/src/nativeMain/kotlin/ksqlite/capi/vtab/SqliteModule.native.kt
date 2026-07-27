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
@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.ClosableStruct
import ksqlite.capi.vtab.SqliteModuleKind.Eponymous
import ksqlite.capi.vtab.SqliteModuleKind.EponymousOnly
import ksqlite.capi.vtab.SqliteModuleKind.Regular

public actual class sqlite3_module<AppData> private constructor(
    internal val callbacks: VtabModuleCallbacks<AppData, *, *>,
    override val pointer: CPointer<s3_module>
) : ClosableStruct(pointer),
    AutoCloseable {

    internal actual constructor(
        version: Int,
        callbacks: VtabModuleCallbacks<AppData, *, *>
    ) : this(callbacks, allocate<s3_module> {
        iVersion = version

        xCreate = when (callbacks.moduleKind) {
            EponymousOnly -> null
            Eponymous -> VtabConnectHandler
            Regular -> VtabCreateHandler
        }

        xConnect = VtabConnectHandler
        xBestIndex = VtabBestIndexHandler
        xDisconnect = VtabDisconnectHandler
        xDestroy = VtabDestroyHandler
        xOpen = VtabOpenHandler
        xClose = VtabCloseHandler
        xFilter = VtabFilterHandler
        xNext = VtabNextHandler
        xEof = VtabEofHandler
        xColumn = VtabColumnHandler
        xRowid = VtabRowidHandler
        xUpdate = callbacks.update?.let { VtabUpdateHandler }
        xBegin = callbacks.begin?.let { VtabBeginHandler }
        xSync = callbacks.sync?.let { VtabSyncHandler }
        xCommit = callbacks.commit?.let { VtabCommitHandler }
        xRollback = callbacks.rollback?.let { VtabRollbackHandler }
        xFindFunction = callbacks.findFunction?.let { VtabFindFunctionHandler }
        xRename = callbacks.rename?.let { VtabRenameHandler }
        xSavepoint = callbacks.savepoint?.let { VtabSavepointHandler }
        xRelease = callbacks.release?.let { VtabReleaseHandler }
        xRollbackTo = callbacks.rollbackTo?.let { VtabRollbackToHandler }
        xShadowName = null
        xIntegrity = callbacks.integrity?.let { VtabIntegrityHandler }
    })
}