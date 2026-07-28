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
package ksqlite.capi.vtab

import ksqlite.capi.createFunction
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.handlers.callbackHandler
import ksqlite.capi.handlers.destructorHandler
import ksqlite.capi.memory.toArray
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_value
import ksqlite.foreign.JniPointer
import ksqlite.foreign.JniPointerArray
import ksqlite.foreign.OutputPointer
import ksqlite.foreign.callbacks.DestructorCallback
import ksqlite.foreign.callbacks.FunctionCallback
import ksqlite.foreign.callbacks.VtabModuleCallbacks as JniVtabModuleCallbacks

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.foreign.structs.sqlite3_index_info
internal typealias s3_module = ksqlite.foreign.structs.sqlite3_module
internal typealias s3_vtab = ksqlite.foreign.structs.sqlite3_vtab
internal typealias s3_vtab_cursor = ksqlite.foreign.structs.sqlite3_vtab_cursor

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

/**
 * Handles all callbacks of a Virtual Table module.
 */
internal object VtabModuleHandler : JniVtabModuleCallbacks {

    override fun create(
        db: JniPointer,
        appData: Any?,
        arguments: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString
    ): Int = vTabCreate(
        module = appData as VtabModule<*, *, *>,
        db = sqlite3(db),
        argv = arguments,
        setVtab = { outVtab.value = it.pointer },
        setError = outErrMsg::value::set
    )

    override fun connect(
        db: JniPointer,
        appData: Any?,
        arguments: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString
    ): Int = vTabCreate(
        module = appData as VtabModule<*, *, *>,
        db = sqlite3(db),
        argv = arguments,
        setVtab = { outVtab.value = it.pointer },
        setError = outErrMsg::value::set
    )

    override fun bestIndex(
        vTab: JniPointer,
        info: JniPointer
    ): Int = vTabBestIndex(
        vTab = vTab,
        info = sqlite3_index_info(info)
    )

    override fun disconnect(vTab: JniPointer): Int = vTabDisconnect(vTab = vTab)

    override fun destroy(vTab: JniPointer): Int = vTabDestroy(vTab = vTab)

    override fun open(
        vTab: JniPointer,
        outCursor: OutputPointer.OfPointer
    ): Int = vTabOpen(
        vTab = vTab,
        setCursor = { outCursor.value = it.pointer }
    )

    override fun close(
        vTab: JniPointer,
        cursor: JniPointer
    ): Int = vTabClose(
        vTab = vTab,
        cursor = cursor
    )

    override fun filter(
        vTab: JniPointer,
        cursor: JniPointer,
        idxNum: Int,
        idxStr: String?,
        argv: JniPointerArray
    ): Int = vTabFilter(
        vTab = vTab,
        cursor = cursor,
        idxNum = idxNum,
        idxStr = idxStr,
        arguments = argv.toArray(::sqlite3_value),
    )

    override fun next(
        vTab: JniPointer,
        cursor: JniPointer
    ): Int = vTabNext(
        vTab = vTab,
        cursor = cursor,
    )

    override fun eof(
        vTab: JniPointer,
        cursor: JniPointer
    ): Int = vTabEof(
        vTab = vTab,
        cursor = cursor,
    )

    override fun column(
        vTab: JniPointer,
        cursor: JniPointer,
        context: JniPointer,
        columnIndex: Int
    ): Int = vTabColumn(
        vTab = vTab,
        cursor = cursor,
        context = sqlite3_context(context),
        columnIndex = columnIndex
    )

    override fun rowid(
        vTab: JniPointer,
        cursor: JniPointer,
        outRowid: OutputPointer.OfInt64
    ): Int = vTabRowid(
        vTab = vTab,
        cursor = cursor,
        setRowid = outRowid::value::set
    )

    override fun update(
        vTab: JniPointer,
        argv: JniPointerArray,
        outRowid: OutputPointer.OfInt64
    ): Int = vTabUpdate(
        vTab = vTab,
        arguments = argv.toArray(::sqlite3_value),
        setRowid = outRowid::value::set
    )

    override fun findFunction(
        vTab: JniPointer,
        argc: Int,
        name: String,
        outAppData: OutputPointer.OfObject<Any>,
        outFunction: OutputPointer.OfObject<FunctionCallback.Func>,
        outDestroy: OutputPointer.OfObject<DestructorCallback>
    ): Int = vTabFindFunction(
        vTab = vTab,
        argumentCount = argc,
        functionName = name,
        setFunction = { _, appData, function, destroy ->
            createFunction(appData, function, null, null, destroy) { fn, fnDestroy ->
                outAppData.value = fn
                outFunction.value = callbackHandler(fn, null, ::FunctionFuncHandler)
                outDestroy.value = destructorHandler(fn, fnDestroy)
            }
        }
    )

    override fun begin(vTab: JniPointer): Int = vTabBegin(vTab)

    override fun sync(vTab: JniPointer): Int = vTabSync(vTab)

    override fun commit(vTab: JniPointer): Int = vTabCommit(vTab)

    override fun rollback(vTab: JniPointer): Int = vTabRollback(vTab)

    override fun rename(
        vTab: JniPointer,
        newName: String
    ): Int = vTabRename(
        vTab = vTab,
        newName = newName
    )

    override fun savepoint(
        vTab: JniPointer,
        savepoint: Int
    ): Int = vTabSavepoint(
        vTab = vTab,
        savepoint = savepoint
    )

    override fun release(
        vTab: JniPointer,
        savepoint: Int
    ): Int = vTabRelease(
        vTab = vTab,
        savepoint = savepoint
    )

    override fun rollbackTo(
        vTab: JniPointer,
        savepoint: Int
    ): Int = vTabRollbackTo(
        vTab = vTab,
        savepoint = savepoint
    )

    override fun integrity(
        vTab: JniPointer,
        schema: String,
        tableName: String,
        flags: Int,
        outError: OutputPointer.OfString
    ): Int = vTabIntegrity(
        vTab = vTab,
        schema = schema,
        tableName = tableName,
        flags = flags,
        setError = outError::value::set
    )
}