package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.wasm
import kotlin.js.toLong

/**
 * Pointer to a static function disposing a [Disposable] registered with [registerGlobalDisposable].
 */
private val GlobalDisposer: WasmPointer =
    wasm.installReferenceFunction { disposeGlobal(it.orNull?.toLong()) }

/**
 * Returns [GlobalDisposer] or [NullPtr] if [data] is `null`.
 */
internal fun globalDisposer(data: Any?): WasmPointer =
    GlobalDisposer.takeIf { data != null } ?: NullPtr

/**
 * Registers a [Disposable] which will invoke [destructor] when disposed.
 * If [destructor] is `null` then `null` is returned.
 */
internal fun bufferDisposer(
    buffer: Buffer,
    destructor: Sqlite3DestroyCallback<Buffer>?
) = instanceDisposer(
    disposer = GlobalDisposer,
    instance = buffer,
    address = buffer.address,
    destructor = destructor,
).notNull