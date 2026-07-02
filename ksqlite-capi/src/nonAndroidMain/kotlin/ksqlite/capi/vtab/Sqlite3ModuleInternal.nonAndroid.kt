@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.vtab

import ksqlite.capi.memory.destroyMemory

/**
 * Invokes the [VtabModuleCallbacks.disconnect] and destroys all the associated resources.
 */
internal inline fun vTabDisconnect(vTab: Long) = vTabDisconnect(vTab, sqlite3_vtab::destroyMemory)

/**
 * Invokes the [VtabModuleCallbacks.destroy] and destroys all the associated resources.
 */
internal inline fun vTabDestroy(vTab: Long) = vTabDestroy(vTab, sqlite3_vtab::destroyMemory)