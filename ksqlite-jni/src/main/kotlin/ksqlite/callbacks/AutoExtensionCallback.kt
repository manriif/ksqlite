package ksqlite.callbacks

import ksqlite.OutputPointer

/**
 * Callback for use with [ksqlite.ksqlite_auto_extension].
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