@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.vtab

import ksqlite.capi.memory.destroyMemory

/**
 * Cleanups the [vTab], releasing overloaded functions if any.
 */
internal inline fun cleanupVtab(vTab: sqlite3_vtab) {
    vTab.destroyMemory()
    vTab.free()
}

/**
 * Invokes the [VtabModuleCallbacks.disconnect] and destroys all the associated resources.
 */
internal inline fun vTabDisconnect(vTab: Long) = vTabDisconnect(vTab, ::cleanupVtab)

/**
 * Invokes the [VtabModuleCallbacks.destroy] and destroys all the associated resources.
 */
internal inline fun vTabDestroy(vTab: Long) = vTabDestroy(vTab, ::cleanupVtab)