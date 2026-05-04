package ksqlite.capi

import ksqlite.capi.interop.Sqlite3
import ksqlite.capi.interop.setSqlite3Instance
import ksqlite.sqliteInitializer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.asJsException
import kotlin.js.unsafeCast

/**
 * Loads and initializes SQLite.
 *
 * This function must be called once before any other API call.
 * No action is performed to ensure that the function has been called only once.
 *
 * A [debugModule] can be supplied to receives initialization outputs.
 *
 * If the sqlite wasm file should be loaded in a non-conventional way, [locateFile] must be supplied
 * and will be invoked to obtain the uri to the file.
 */
public suspend fun initializeSqlite(
    debugModule: ((args: JsArray<out JsAny>) -> Unit)? = null,
    locateFile: ((path: String, prefix: String) -> JsAny?)? = null
) {
    setSqlite3Instance(suspendCoroutine { continuation ->
        @Suppress("ThrowableNotThrown", "RemoveExplicitTypeArguments")
        val _ = sqliteInitializer(
            debugModule = debugModule,
            locateFile = locateFile
        ).then(
            onFulfilled = { continuation.resume(it.unsafeCast<Sqlite3>()); null },
            onRejected = { continuation.resumeWithException(it.asJsException()); null }
        )
    })
}