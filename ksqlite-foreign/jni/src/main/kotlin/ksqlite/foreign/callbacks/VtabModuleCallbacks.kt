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
package ksqlite.foreign.callbacks

import ksqlite.foreign.JniPointer
import ksqlite.foreign.JniPointerArray
import ksqlite.foreign.OutputPointer

/**
 * Regroups all the callbacks of the `sqlite3_module` interface.
 * All the functions are invoked from JNI.
 */
public interface VtabModuleCallbacks {

    public fun create(
        db: JniPointer,
        appData: Any?,
        arguments: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString,
    ): Int

    public fun connect(
        db: JniPointer,
        appData: Any?,
        arguments: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString,
    ): Int

    public fun bestIndex(
        vTab: JniPointer,
        info: JniPointer
    ): Int

    public fun disconnect(vTab: JniPointer): Int

    public fun destroy(vTab: JniPointer): Int

    public fun open(
        vTab: JniPointer,
        outCursor: OutputPointer.OfPointer
    ): Int

    public fun close(
        vTab: JniPointer,
        cursor: JniPointer
    ): Int

    public fun filter(
        vTab: JniPointer,
        cursor: JniPointer,
        idxNum: Int,
        idxStr: String?,
        argv: JniPointerArray // [sqlite3_value]
    ): Int

    public fun next(
        vTab: JniPointer,
        cursor: JniPointer
    ): Int

    public fun eof(
        vTab: JniPointer,
        cursor: JniPointer
    ): Int

    public fun column(
        vTab: JniPointer,
        cursor: JniPointer,
        context: JniPointer,
        columnIndex: Int
    ): Int

    public fun rowid(
        vTab: JniPointer,
        cursor: JniPointer,
        outRowid: OutputPointer.OfInt64
    ): Int

    public fun update(
        vTab: JniPointer,
        argv: JniPointerArray, // [sqlite3_value]
        outRowid: OutputPointer.OfInt64
    ): Int

    public fun begin(vTab: JniPointer): Int

    public fun sync(vTab: JniPointer): Int

    public fun commit(vTab: JniPointer): Int

    public fun rollback(vTab: JniPointer): Int

    public fun findFunction(
        vTab: JniPointer,
        argc: Int,
        name: String,
        outAppData: OutputPointer.OfObject<Any>,
        outFunction: OutputPointer.OfObject<FunctionCallback.Func>,
        outDestroy: OutputPointer.OfObject<DestructorCallback>
    ): Int

    public fun rename(
        vTab: JniPointer,
        newName: String
    ): Int

    public fun savepoint(
        vTab: JniPointer,
        savepoint: Int
    ): Int

    public fun release(
        vTab: JniPointer,
        savepoint: Int
    ): Int

    public fun rollbackTo(
        vTab: JniPointer,
        savepoint: Int
    ): Int

    public fun integrity(
        vTab: JniPointer,
        schema: String,
        tableName: String,
        flags: Int,
        outError: OutputPointer.OfString
    ): Int
}