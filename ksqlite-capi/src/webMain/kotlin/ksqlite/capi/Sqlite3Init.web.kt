package ksqlite.capi

import ksqlite.capi.interop.Sqlite3
import ksqlite.capi.interop.Sqlite3Wasm
import ksqlite.capi.interop.Sqlite3WasmExports
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
 * Returns the [Sqlite3Wasm] instance.
 */
internal inline val wasm: Sqlite3Wasm
    get() = sqlite3.wasm

/**
 * Returns the [Sqlite3Wasm] instance.
 */
internal inline val exports: Sqlite3WasmExports
    get() = wasm.exports

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
 *
 * @throws IllegalStateException if the library was already initialized.
 */
public suspend fun initializeSqlite(
    debugModule: ((args: JsArray<out JsAny>) -> Unit)? = null,
    locateFile: ((path: String, prefix: String) -> JsAny?)? = null
) {
    check(Sqlite3Instance == null) {
        "Sqlite3 is already initialized"
    }

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