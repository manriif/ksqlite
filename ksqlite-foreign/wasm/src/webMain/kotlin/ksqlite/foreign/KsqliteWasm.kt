package ksqlite.foreign

import ksqlite.foreign.structs.WasmStructLayoutProvider
import ksqlite.structs.setStructLayoutProvider

private var Initialized = false

/**
 * Initializes top level stuff to use the library.
 * Only the first call as effect, subsequent ones are no-op.
 */
public fun ksqliteInitLibrary() {
    if (!Initialized) {
        setStructLayoutProvider(WasmStructLayoutProvider)
        Initialized = true
    }
}