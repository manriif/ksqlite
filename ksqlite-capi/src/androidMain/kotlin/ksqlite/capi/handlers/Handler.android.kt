package ksqlite.capi.handlers

import ksqlite.DestructorCallback
import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Handler for native callback.
 */
internal abstract class Handler<Data : Any>(private val holder: Holder<Data>) {

    /**
     * Returns [block]'s result, invoked with [Data] and optional userData.
     */
    protected inline fun <Result> handler(
        block: (data: Data, userData: sqlite3_mutable_pointer?) -> Result
    ): Result {
        return block(holder.data, holder.userData)
    }

    /**
     * Object to be passed as `user_data` on native side.
     */
    data class Holder<Data : Any>(
        val data: Data,
        val userData: sqlite3_mutable_pointer?
    )
}

/**
 * Handler for [Sqlite3DestructorCallback].
 */
private class DestructorHandler(holder: Holder<Sqlite3DestructorCallback>) :
    Handler<Sqlite3DestructorCallback>(holder),
    DestructorCallback {

    override fun destroy() = handler { callback, userData ->
        callback(userData)
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [Handler] instance supplied by [factory] only if [data] is not `null`.
 */
internal fun <Data : Any, H : Handler<Data>> callbackHandler(
    data: Data?,
    userData: sqlite3_mutable_pointer? = null,
    factory: (Handler.Holder<Data>) -> H
): H? {
    if (data == null) {
        return null
    }

    return factory(Handler.Holder(data, userData))
}

/**
 * Returns a [DestructorCallback] if [destructor] is not `null`.
 */
internal fun destructorHandler(
    destructor: Sqlite3DestructorCallback?,
    userData: sqlite3_mutable_pointer? = null
): DestructorCallback? {
    if (destructor == null) {
        return null
    }

    return callbackHandler(
        data = destructor,
        userData = userData,
        factory = ::DestructorHandler
    )
}