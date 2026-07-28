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

import ksqlite.foreign.wasm.WasmPointer
import ksqlite.structs.ksqlite_cipher_descriptor
import ksqlite.structs.ksqlite_cipher_params
import ksqlite.structs.sqlite3_file
import ksqlite.structs.sqlite3_index_info
import ksqlite.structs.sqlite3_io_methods
import ksqlite.structs.sqlite3_module
import ksqlite.structs.sqlite3_vfs
import ksqlite.structs.sqlite3_vtab
import ksqlite.structs.sqlite3_vtab_cursor

/**
 * Allocates a `sqlite3_file` with given [size].
 */
public class sqlite3_file(size: Int) : sqlite3_file<WasmPointer>(WasmStructAdapter, null, size)

/**
 * Reinterprets a `sqlite3_index_info`.
 */
public class sqlite3_index_info(pointer: WasmPointer) :
    sqlite3_index_info<WasmPointer>(WasmStructAdapter, pointer)

/**
 * Reinterprets a `sqlite3_io_methods`.
 */
public class sqlite3_io_methods(pointer: WasmPointer) :
    sqlite3_io_methods<WasmPointer>(WasmStructAdapter, pointer)

/**
 * Allocates a `sqlite3_module`.
 */
public class sqlite3_module : sqlite3_module<WasmPointer>(WasmStructAdapter, null)

/**
 * Reinterprets a `sqlite3_vfs`.
 */
public class sqlite3_vfs(pointer: WasmPointer) :
    sqlite3_vfs<WasmPointer>(WasmStructAdapter, pointer)

/**
 * Allocates a `sqlite3_vtab`.
 */
public class sqlite3_vtab : sqlite3_vtab<WasmPointer>(WasmStructAdapter, null)

/**
 * Allocates a `sqlite3_vtab_cursor`.
 */
public class sqlite3_vtab_cursor : sqlite3_vtab_cursor<WasmPointer>(WasmStructAdapter, null)

/**
 * Allocates a `ksqlite_cipher_descriptor`.
 */
public class ksqlite_cipher_descriptor :
    ksqlite_cipher_descriptor<WasmPointer>(WasmStructAdapter, null)

/**
 * Allocates or reinterprets a `ksqlite_cipher_params`.
 */
public class ksqlite_cipher_params(pointer: WasmPointer?) :
    ksqlite_cipher_params<WasmPointer>(WasmStructAdapter, pointer)