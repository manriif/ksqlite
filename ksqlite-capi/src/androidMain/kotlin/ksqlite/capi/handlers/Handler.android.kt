package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.DestructorCallback
import ksqlite.capi.callbacks.SqliteDestroyCallback

/**
 * Handler for native callback.
 */
internal abstract class Handler<Data : Any, AppData> {

    lateinit var holder: Holder<Data, AppData>

    /**
     * Returns [block]'s result, invoked with [Data] and optional appData.
     */
    protected inline fun <Result> handle(
        block: (data: Data, appData: AppData) -> Result
    ): Result {
        return block(holder.data, holder.appData)
    }

    /**
     * Object to be passed as `user_data` on native side.
     */
    data class Holder<Data : Any, AppData>(
        val data: Data,
        val appData: AppData
    )
}

/**
 * Handler for [SqliteDestroyCallback].
 */
private class DestructorHandler<AppData> :
    Handler<SqliteDestroyCallback<AppData>, AppData>(),
    DestructorCallback {

    override fun apply() = handle { callback, appData ->
        callback.apply(appData)
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [Handler] instance supplied by [factory] only if [data] is not `null`.
 */
internal fun <Data : Any, AppData, H : Handler<Data, AppData>> callbackHandler(
    data: Data?,
    appData: AppData,
    factory: () -> H
): H? {
    if (data == null) {
        return null
    }

    return factory().apply {
        holder = Handler.Holder(data, appData)
    }
}

/**
 * Returns a [DestructorCallback] if [destructor] is not `null`.
 */
internal fun <AppData> destructorHandler(
    appData: AppData,
    destructor: SqliteDestroyCallback<AppData>?
): DestructorCallback? {
    if (destructor == null) {
        return null
    }

    return callbackHandler(
        data = destructor,
        appData = appData,
        factory = ::DestructorHandler
    )
}