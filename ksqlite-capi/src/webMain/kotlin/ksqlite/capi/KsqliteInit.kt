package ksqlite.capi

import ksqlite.Sqlite3
import ksqlite.sqliteInitializer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.asJsException
import kotlin.js.unsafeCast

/**
 * One single SQLite instance is allowed per application session.
 */
private var Sqlite3Instance: Sqlite3? = null

/**
 * Returns the [Sqlite3] instance or raise an error if SQLite wasn't initialized.
 */
internal val sqlite3: Sqlite3
    get() = checkNotNull(Sqlite3Instance) {
        "SQLite was not initialized, function initializeSqlite() must be called before any other " +
                "API call"
    }

/**
 * Loads SQLite.
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
    Sqlite3Instance = suspendCoroutine { continuation ->
        @Suppress("ThrowableNotThrown", "RemoveExplicitTypeArguments")
        val _ = sqliteInitializer(
            debugModule = debugModule,
            locateFile = locateFile
        ).then(
            onFulfilled = { continuation.resume(it.unsafeCast<Sqlite3>()); null },
            onRejected = { continuation.resumeWithException(it.asJsException()); null }
        )
    }
}