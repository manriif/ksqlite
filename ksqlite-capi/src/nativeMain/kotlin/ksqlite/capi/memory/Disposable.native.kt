package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toLong
import ksqlite.capi.callbacks.Sqlite3DestroyCallback

/**
 * C-static function disposing a [Disposable] registered with [registerGlobalDisposable].
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val GlobalDisposer = staticCFunction { pointer: COpaquePointer? ->
    disposeGlobal(pointer?.toLong())
}

/**
 * Returns [GlobalDisposer] only if [data] != `null`.
 */
internal fun globalDisposer(data: Any?) = GlobalDisposer.takeIf { data != null }

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
)