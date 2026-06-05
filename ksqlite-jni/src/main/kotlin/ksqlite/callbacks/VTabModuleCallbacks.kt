package ksqlite.callbacks

import ksqlite.OutputPointer

/**
 * Regroups all the callback of the Virtual Table module interface.
 * All the functions are invoked from JNI.
 */
public interface VTabModuleCallbacks {

    /**
     * xCreate, returns the
     */
    public fun create(
        db: Long,
        argv: Array<String>,
        ppVtab: OutputPointer.OfPointer,
        pzErrMsg: OutputPointer.OfPointer,
    ): Int

    public fun connect(
        db: Long,
        argv: Array<String>,
        ppVtab: OutputPointer.OfPointer,
        pzErrMsg: OutputPointer.OfPointer,
    )

    public fun bestIndex() {

    }
}