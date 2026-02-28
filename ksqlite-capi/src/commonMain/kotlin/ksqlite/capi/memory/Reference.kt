package ksqlite.capi.memory

import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Reference to an object preventing GC from collecting or moving it.
 */
internal interface Reference : Disposable {

    /**
     * Internally referenced data.
     */
    val data: Any?

    /**
     * The associated user data.
     * For [ByteArray] and [String], it is the content of the underlying type.
     * */
    val userData: sqlite3_mutable_pointer?

    /**
     * Disposes the reference, making referenced object(s) eligible to GC.
     */
    override fun dispose()
}