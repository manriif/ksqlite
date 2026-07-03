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
import kotlin.js.JsBigInt
import kotlin.js.asJsException
import kotlin.js.toJsBigInt

/**
 * One single SQLite instance is allowed per application session.
 */
private var Sqlite3Instance: Sqlite3? = null
private var SQLITE_TRANSIENT: JsBigInt? = null

/**
 * Returns the [Sqlite3] instance or raise an error if SQLite wasn't initialized.
 */
internal val s3: Sqlite3
    get() = checkNotNull(Sqlite3Instance) {
        "SQLite was not initialized, function initializeSqlite() must be called before any other " +
                "API call"
    }

internal val sqliteTransient: JsBigInt
    get() = checkNotNull(SQLITE_TRANSIENT)

/**
 * Returns the [Sqlite3Capi] instance.
 */
internal inline val capi: Sqlite3Capi
    get() = s3.capi

/**
 * Returns the [Sqlite3Wasm] instance.
 */
internal inline val wasm: Sqlite3Wasm
    get() = s3.wasm

/**
 * Returns the [Sqlite3WasmExports] instance.
 */
internal inline val exports: Sqlite3WasmExports
    get() = wasm.exports

/**
 * Whether a call to [sqliteLoad] has been made and was successful.
 */
public val isSqliteLoaded: Boolean
    get() = Sqlite3Instance != null

/**
 * Configuration for the SQLite loader.
 */
public interface SqliteLoaderConfig {

    /**
     * Handler for initialization outputs.
     */
    public var debugModule: ((args: JsArray<out JsAny?>) -> Unit)?

    /**
     * Handler for file location.
     *
     * If file(s) such as the `ksqlite.wasm` file should be loaded in a non-conventional way, then
     * this property must be non-null and is invoked to obtain the uri to the file.
     */
    public var fileLocator: ((path: String, prefix: String) -> JsAny?)?
}

private class SqliteLoaderConfigImpl(
    override var debugModule: ((args: JsArray<out JsAny?>) -> Unit)? = null,
    override var fileLocator: ((path: String, prefix: String) -> JsAny?)? = null
) : SqliteLoaderConfig

/**
 * Loads the SQLite library without initializing it.
 * The load process can be customized within [configure].
 *
 * This function must be called once before any other API call.
 *
 * @throws IllegalStateException if the library was already loaded.
 */
public suspend fun sqliteLoad(configure: (SqliteLoaderConfig.() -> Unit)? = null) {
    check(Sqlite3Instance == null) {
        "SQLite is already initialized"
    }

    val loader = configure?.run {
        SqliteLoaderConfigImpl().apply(this::invoke)
    }

    Sqlite3Instance = suspendCoroutine { continuation ->
        @Suppress("ThrowableNotThrown")
        val _ = ksqliteLoadLibrary(
            debugModule = loader?.debugModule,
            locateFile = loader?.fileLocator
        ).then(
            onFulfilled = { continuation.resume(it); null },
            onRejected = { continuation.resumeWithException(it.asJsException()); null }
        )
    }

    SQLITE_TRANSIENT = capi.SQLITE_TRANSIENT.toLong().toJsBigInt()
}