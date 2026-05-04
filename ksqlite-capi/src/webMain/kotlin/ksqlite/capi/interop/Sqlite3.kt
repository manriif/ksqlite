package ksqlite.capi.interop

import ksqlite.capi.interop.api.Sqlite3Capi
import ksqlite.capi.interop.wasm.Sqlite3Wasm
import kotlin.js.JsAny

/**
 * Object constructed by the [ksqlite.sqliteInitModule] function.
 */
internal external interface Sqlite3 : JsAny {

    /**
     * The namespace for the C-style APIs.
     */
    val capi: Sqlite3Capi

    /**
     * WASM-specific utilities, abstracted to be independent of and configurable for use with,
     * arbitrary WASM runtime environments.
     */
    val wasm: Sqlite3Wasm
}

///////////////////////////////////////////////////////////////////////////
// Instance
///////////////////////////////////////////////////////////////////////////

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
 * Sets the SQLite3 instance for the whole application lifecycle.
 *
 * @throws IllegalStateException if an instance was already set.
 */
internal fun setSqlite3Instance(instance: Sqlite3) {
    check(Sqlite3Instance == null) {
        "Sqlite3 is already initialized"
    }

    Sqlite3Instance = instance
}