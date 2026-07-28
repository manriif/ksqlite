/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ClassName")

package ksqlite.foreign.structs

import ksqlite.foreign.JniPointer
import ksqlite.foreign.callbacks.CipherDescriptorCallbacks
import ksqlite.foreign.callbacks.VtabModuleCallbacks
import ksqlite.foreign.cipherDescriptorInstall
import ksqlite.foreign.cipherDescriptorUninstall
import ksqlite.foreign.moduleDeinit
import ksqlite.foreign.moduleInit
import ksqlite.foreign.vTabDeinit
import ksqlite.foreign.vTabInit

/**
 * Allocates a `sqlite3_file` with given [size].
 */
public class sqlite3_file(size: Int) :
    ksqlite.structs.sqlite3_file<JniPointer>(JniStructAdapter, null, size)

/**
 * Reinterprets a `sqlite3_index_info`.
 */
public class sqlite3_index_info(pointer: JniPointer) :
    ksqlite.structs.sqlite3_index_info<JniPointer>(JniStructAdapter, pointer)

/**
 * Reinterprets a `sqlite3_io_methods`.
 */
public class sqlite3_io_methods(pointer: JniPointer) :
    ksqlite.structs.sqlite3_io_methods<JniPointer>(JniStructAdapter, pointer)

/**
 * Allocates a `sqlite3_module`.
 */
public class sqlite3_module(
    callbacks: VtabModuleCallbacks,
    eponymous: Boolean,
    optionalCallbacks: Set<Member>
) : ksqlite.structs.sqlite3_module<JniPointer>(JniStructAdapter, null) {

    init {
        val callbackMask = optionalCallbacks.fold(0) { mask, member ->
            mask or (1 shl member.ordinal)
        }

        moduleInit(pointer, callbackMask, eponymous, callbacks)
    }

    override fun free() {
        moduleDeinit(pointer)
        super.free()
    }
}

/**
 * Reinterprets a `sqlite3_vfs`.
 */
public class sqlite3_vfs(pointer: JniPointer) :
    ksqlite.structs.sqlite3_vfs<JniPointer>(JniStructAdapter, pointer)

/**
 * Allocates a `sqlite3_vtab`.
 */
public class sqlite3_vtab : ksqlite.structs.sqlite3_vtab<JniPointer>(JniStructAdapter, null) {

    init {
        vTabInit(pointer)
    }

    override fun free() {
        vTabDeinit(pointer)
        super.free()
    }
}

/**
 * Allocates a `sqlite3_vtab_cursor`.
 */
public class sqlite3_vtab_cursor :
    ksqlite.structs.sqlite3_vtab_cursor<JniPointer>(JniStructAdapter, null)

/**
 * Allocates a `ksqlite_cipher_descriptor`.
 */
public class ksqlite_cipher_descriptor(private val callbacks: CipherDescriptorCallbacks<*>) :
    ksqlite.structs.ksqlite_cipher_descriptor<JniPointer>(JniStructAdapter, null) {

    private var slot: Int? = null

    /**
     * Installs this descriptor at the given slot [index].
     */
    public fun install(index: Int) {
        cipherDescriptorInstall(pointer, index, callbacks)
        slot = index
    }

    /**
     * Uninstall this descriptor from the slot it was installed at.
     */
    public fun uninstall() {
        slot?.let { index ->
            cipherDescriptorUninstall(pointer, index)
        }
    }

    override fun free() {
        uninstall()
        super.free()
    }
}

/**
 * Allocates or reinterprets a `ksqlite_cipher_params`.
 */
public class ksqlite_cipher_params(pointer: JniPointer?) :
    ksqlite.structs.ksqlite_cipher_params<JniPointer>(JniStructAdapter, pointer)