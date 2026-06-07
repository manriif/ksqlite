package ksqlite.callbacks

import ksqlite.OutputPointer

/**
 * Regroups all the callback of the Virtual Table module interface.
 * All the functions are invoked from JNI.
 */
public interface VTabModuleCallbacks {

    public fun create(
        db: Long,
        appData: Any?,
        arguments: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString,
    ): Int

    public fun connect(
        db: Long,
        appData: Any?,
        arguments: Array<String>,
        outVtab: OutputPointer.OfPointer,
        outErrMsg: OutputPointer.OfString,
    ): Int

    public fun bestIndex(
        vTab: Long,
        info: Long
    ): Int

    public fun disconnect(vTab: Long): Int

    public fun destroy(vTab: Long): Int

    public fun open(
        vTab: Long,
        outCursor: OutputPointer.OfPointer
    ): Int

    public fun close(
        vTab: Long,
        cursor: Long
    ): Int

    public fun filter(
        vTab: Long,
        cursor: Long,
        idxNum: Int,
        idxStr: String?,
        argv: LongArray // [sqlite3_value]
    ): Int

    public fun next(
        vTab: Long,
        cursor: Long
    ): Int

    public fun eof(
        vTab: Long,
        cursor: Long
    ): Int

    public fun column(
        vTab: Long,
        cursor: Long,
        context: Long,
        columnIndex: Int
    ): Int

    public fun rowid(
        vTab: Long,
        cursor: Long,
        outRowid: OutputPointer.OfInt64
    ): Int

    public fun update(
        vTab: Long,
        argv: LongArray, // [sqlite3_value]
        outRowid: OutputPointer.OfInt64
    ): Int

    public fun begin(vTab: Long): Int

    public fun sync(vTab: Long): Int

    public fun commit(vTab: Long): Int

    public fun rollback(vTab: Long): Int

    public fun findFunction(
        vTab: Long,
        argc: Int,
        name: String,
        outAppData: OutputPointer.OfObject<Any>,
        outFunction: OutputPointer.OfObject<FunctionCallback.Func>,
        outDestroy: OutputPointer.OfObject<DestructorCallback>
    ): Int

    public fun rename(
        vTab: Long,
        newName: String
    ): Int

    public fun savepoint(
        vTab: Long,
        savepoint: Int
    ): Int

    public fun release(
        vTab: Long,
        savepoint: Int
    ): Int

    public fun rollbackTo(
        vTab: Long,
        savepoint: Int
    ): Int

    public fun integrity(
        vTab: Long,
        schema: String,
        tableName: String,
        flags: Int,
        outError: OutputPointer.OfString
    ): Int
}