package ksqlite

import org.sqlite.jni.annotation.Nullable

/**
 * Callback for use with [sqlite3_wal_hook].
 */
public fun interface WalCallback {

    /**
     * Invoked from JNI.
     */
    public fun call(): Int
}