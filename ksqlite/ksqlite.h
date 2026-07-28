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
#ifndef KSQLITE_H
#define KSQLITE_H

#ifndef __WASM__

#include "sqlite3.h"

#endif

#ifdef __cplusplus
extern "C" {
#endif

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

/**
 * Exposes the SQLITE_TRANSIENT macro as a constant so it can be referenced
 * from Kotlin/Native when using direct ccall mode.
 */
__attribute__((unused))
extern const sqlite3_destructor_type KSQLITE_TRANSIENT;

///////////////////////////////////////////////////////////////////////////
// Typedefs
///////////////////////////////////////////////////////////////////////////

/**
 * Holds the layout of a structure.
 */
typedef int* ksqlite_layout;

/**
 * Exposes the `CipherDescriptor` in a way some generator can handle it.
 */
__attribute__((unused))
typedef struct _CipherDescriptor ksqlite_cipher_descriptor;

/**
 * Exposes the `CipherParams` in a way some generator can handle it.
 */
__attribute__((unused))
typedef struct _CipherParams ksqlite_cipher_params;

/**
 * Callback for SQLITE_CONFIG_LOG, exposed for binding generation.
 */
__attribute__((unused))
typedef void(* ksqlite_xLog)(void*, int, const char*);

/**
 * Callback for SQLITE_CONFIG_SQLLOG, exposed for binding generation.
 */
__attribute__((unused))
typedef void(* ksqlite_xSqllog)(void*, sqlite3*, const char*, int);

/**
 * Callback expected by sqlite3_auto_extension() and sqlite3_cancel_auto_extension() with the
 * full signature.
 */
typedef int (* ksqlite_xEntryPoint)(
    sqlite3* db,
    char** pzErrMsg,
    const struct sqlite3_api_routines* pThunk
);

///////////////////////////////////////////////////////////////////////////
// Layout
///////////////////////////////////////////////////////////////////////////

/**
 * Recognized struct types.
 */
enum ksqlite_struct_type : int {
    Sqlite3IndexInfo = 0,
    Sqlite3IndexConstraint = 1,
    Sqlite3IndexConstraintUsage = 2,
    Sqlite3IndexOrderby = 3,
    Sqlite3Module = 4,
    Sqlite3Vtab = 5,
    Sqlite3VtabCursor = 6,
    Sqlite3File = 7,
    Sqlite3IoMethods = 8,
    Sqlite3Vfs = 9,
    KsqliteCipherDescriptor = 10,
    KsqliteCipherParams = 11
};

/**
 * Allocates and returns the layout of the struct identified by `structType`.
 *
 * For a struct member index M:
 *
 * - array[M*2] = offset
 * - array[M*2+1] = length
 *
 * The struct's total size can be obtained by reading the last element of the returned array.
 * The offset and length are written in the order they're declared in the C struct.
 *
 * The returned `ksqlite_layout` must be freed when no longer required by passing it to
 * `ksqlite_struct_layout_free`.
 */
ksqlite_layout ksqlite_struct_layout_allocate(
    enum ksqlite_struct_type structType,
    int* layoutSize
);

/**
 * Frees a `ksqlite_layout` instance previously obtained via `ksqlite_struct_layout_allocate`
 * @param layout
 */
void ksqlite_struct_layout_free(ksqlite_layout layout);

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * Wrappers function around sqlite3_auto_extension() with accept the xEntryPoint parameter with the
 * signature expected by SQLite. This is necessary for interop tools to generate compatible
 * code.
 *
 * @param xEntryPoint the XEntryPoint with expected signature.
 * @return sqlite3_auto_extension() result
 */
int ksqlite_auto_extension(ksqlite_xEntryPoint);

/**
 * Wrappers function around sqlite3_cancel_auto_extension() with accept the xEntryPoint parameter
 * with the signature expected by SQLite. This is necessary for interop tools to generate
 * compatible  code.
 *
 * @param xEntryPoint the XEntryPoint with expected signature.
 * @return sqlite3_cancel_auto_extension() result
 */
int ksqlite_cancel_auto_extension(ksqlite_xEntryPoint);

/**
 * Works like the canonical sqlite3_prepare_v2() but its "tail"  output parameter is returned as the
 * index offset into the given byte array at which SQL parsing stopped.
 */
int ksqlite_prepare_v2(
    sqlite3* db,            /* Database handle */
    const char* zSql,       /* SQL statement, UTF-8 encoded */
    int nByte,              /* Maximum length of zSql in bytes. */
    sqlite3_stmt** ppStmt,  /* OUT: Statement handle */
    int* pzTailOffset       /* OUT: Pointer to index of the unused portion of zSql */
);

/**
 * Works like the canonical sqlite3_prepare_v3() but its "tail"  output parameter is returned as the
 * index offset into the given byte array at which SQL parsing stopped.
 */
int ksqlite_prepare_v3(
    sqlite3* db,            /* Database handle */
    const char* zSql,       /* SQL statement, UTF-8 encoded */
    int nByte,              /* Maximum length of zSql in bytes. */
    unsigned int prepFlags, /* Zero or more SQLITE_PREPARE_ flags */
    sqlite3_stmt** ppStmt,  /* OUT: Statement handle */
    int* pzTailOffset       /* OUT: Pointer to index of the unused portion of zSql */
);

#ifdef __cplusplus
}
#endif

#endif // KSQLITE_H