package ksqlite.foreign.callbacks

import ksqlite.foreign.OutputPointer

/**
 * Callback for use with [ksqlite.foreign.ksqlite_auto_extension].
 */
public fun interface AutoExtensionCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply(
        db: Long,
        api: Long,
        outErrMsg: OutputPointer.OfString
    ): Int
}