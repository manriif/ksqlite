package ksqlite.capi

import ksqlite.foreign.Sqlite3
import ksqlite.foreign.Sqlite3Capi
import ksqlite.foreign.Sqlite3Wasm
import ksqlite.foreign.Sqlite3WasmExports
import ksqlite.foreign.ksqliteLoadLibrary
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.asJsException

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
 * Returns the [Sqlite3Capi] instance.
 */
internal inline val capi: Sqlite3Capi
    get() = sqlite3.capi

/**
 * Returns the [Sqlite3Wasm] instance.
 */
internal inline val wasm: Sqlite3Wasm
    get() = sqlite3.wasm

/**
 * Returns the [Sqlite3WasmExports] instance.
 */
internal inline val exports: Sqlite3WasmExports
    get() = wasm.exports

/**
 * Whether a call to [ksqliteLoad] has been made and was successful.
 */
public val isSqliteInitialized: Boolean
    get() = Sqlite3Instance != null

/**
 * Loads the Kotlin SQLite library without initializing it.
 *
 * This function must be called once before any other API call.
 *
 * A [debugModule] can be supplied to receives initialization outputs.
 *
 * If the `ksqlite.wasm` file should be loaded in a non-conventional way, [locateFile] must be
 * supplied and will be invoked to obtain the uri to the file.
 *
 * @throws IllegalStateException if the library was already loaded.
 */
public suspend fun ksqliteLoad(
    debugModule: ((args: JsArray<out JsAny>) -> Unit)? = null,
    locateFile: ((path: String, prefix: String) -> JsAny?)? = null
) {
    check(Sqlite3Instance == null) {
        "Sqlite3 is already initialized"
    }

    Sqlite3Instance = suspendCoroutine { continuation ->
        @Suppress("ThrowableNotThrown")
        val _ = ksqliteLoadLibrary(
            debugModule = debugModule,
            locateFile = locateFile
        ).then(
            onFulfilled = { continuation.resume(it); null },
            onRejected = { continuation.resumeWithException(it.asJsException()); null }
        )
    }
}