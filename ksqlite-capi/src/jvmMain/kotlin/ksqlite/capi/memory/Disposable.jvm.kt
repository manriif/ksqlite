package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import java.lang.foreign.MemorySegment

/**
 * Pointer to a static function disposing a [Disposable] registered with [registerGlobalDisposable].
 */
private val GlobalDisposer: MemorySegment =
    StaticMemoryAllocator.allocateReferenceFunction { disposeGlobal(it.orNull?.address()) }

/**
 * Returns [GlobalDisposer] or [NullPtr] if [data] is `null`.
 */
internal fun globalDisposer(data: Any?) =
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