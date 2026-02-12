@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin

public actual class pointer(internal val pointer: COpaquePointer)

public actual class sqlite3_context(internal val pointer: CPointer<cnames.structs.sqlite3_context>)

public actual class sqlite3_stmt(internal val pointer: CPointer<cnames.structs.sqlite3_stmt>) {

    private val lazyPins = lazy { mutableListOf<Pinned<*>>() }
    private val pinneds by lazyPins

    internal fun pin(value: ByteArray?): CPointer<ByteVar>? {
        val pinned = value?.pin() ?: return null
        pinneds.add(pinned)
        return pinned.addressOf(0)
    }

    internal fun unpinAll() {
        if (lazyPins.isInitialized()) {
            pinneds.onEach(Pinned<*>::unpin).clear()
        }
    }
}