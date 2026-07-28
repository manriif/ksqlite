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
#ifndef __WASM__

#include "ksqlite.h"

#endif

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

__attribute__((unused))
const sqlite3_destructor_type KSQLITE_TRANSIENT = SQLITE_TRANSIENT;

///////////////////////////////////////////////////////////////////////////
// Layouts
///////////////////////////////////////////////////////////////////////////

#ifndef offsetof
# define offsetof(ST,M) ((int)((char*)&((ST*)0)->M - (char*)0))
#endif

#pragma clang diagnostic push
#pragma ide diagnostic ignored "bugprone-sizeof-expression"

#define StructLayoutBegin(memberCount) \
    static const int arraySize = (memberCount) * 2 + 1; \
    int *buffer = sqlite3_malloc(sizeof(int) * arraySize); \
    if (buffer == 0) return 0; \
    int position = 0

#define StructLayoutAppend(type, member) \
    buffer[position++] = (int) offsetof(type, member); \
    buffer[position++] = (int) sizeof(((type*)0)->member)

#define StructLayoutEnd(type) \
    buffer[position] = (int) sizeof(type); \
    if (layoutSize != 0) *layoutSize = arraySize; \
    return buffer

/**
 * Returns the layout for `sqlite3_index_info`.
 */
static ksqlite_layout struct_layout_sqlite3_index_info(int* layoutSize) {
    StructLayoutBegin(13);
    StructLayoutAppend(sqlite3_index_info, nConstraint);
    StructLayoutAppend(sqlite3_index_info, aConstraint);
    StructLayoutAppend(sqlite3_index_info, nOrderBy);
    StructLayoutAppend(sqlite3_index_info, aOrderBy);
    StructLayoutAppend(sqlite3_index_info, aConstraintUsage);
    StructLayoutAppend(sqlite3_index_info, idxNum);
    StructLayoutAppend(sqlite3_index_info, idxStr);
    StructLayoutAppend(sqlite3_index_info, needToFreeIdxStr);
    StructLayoutAppend(sqlite3_index_info, orderByConsumed);
    StructLayoutAppend(sqlite3_index_info, estimatedCost);
    StructLayoutAppend(sqlite3_index_info, estimatedRows);
    StructLayoutAppend(sqlite3_index_info, idxFlags);
    StructLayoutAppend(sqlite3_index_info, colUsed);
    StructLayoutEnd(sqlite3_index_info);
}

/**
 * Returns the layout for `sqlite3_index_constraint`.
 */
static ksqlite_layout struct_layout_sqlite3_index_constraint(int* layoutSize) {
    StructLayoutBegin(4);
    StructLayoutAppend(struct sqlite3_index_constraint, iColumn);
    StructLayoutAppend(struct sqlite3_index_constraint, op);
    StructLayoutAppend(struct sqlite3_index_constraint, usable);
    StructLayoutAppend(struct sqlite3_index_constraint, iTermOffset);
    StructLayoutEnd(struct sqlite3_index_constraint);
}

/**
 * Returns the layout for `sqlite3_index_constraint_usage`.
 */
static ksqlite_layout struct_layout_sqlite3_index_constraint_usage(int* layoutSize) {
    StructLayoutBegin(2);
    StructLayoutAppend(struct sqlite3_index_constraint_usage, argvIndex);
    StructLayoutAppend(struct sqlite3_index_constraint_usage, omit);
    StructLayoutEnd(struct sqlite3_index_constraint_usage);
}

/**
 * Returns the layout for `sqlite3_index_orderby`.
 */
static ksqlite_layout struct_layout_sqlite3_index_order_by(int* layoutSize) {
    StructLayoutBegin(2);
    StructLayoutAppend(struct sqlite3_index_orderby, iColumn);
    StructLayoutAppend(struct sqlite3_index_orderby, desc);
    StructLayoutEnd(struct sqlite3_index_orderby);
}

/**
 * Returns the layout for `sqlite3_module`.
 */
static ksqlite_layout struct_layout_sqlite3_module(int* layoutSize) {
    StructLayoutBegin(25);
    StructLayoutAppend(sqlite3_module, iVersion);
    StructLayoutAppend(sqlite3_module, xCreate);
    StructLayoutAppend(sqlite3_module, xConnect);
    StructLayoutAppend(sqlite3_module, xBestIndex);
    StructLayoutAppend(sqlite3_module, xDisconnect);
    StructLayoutAppend(sqlite3_module, xDestroy);
    StructLayoutAppend(sqlite3_module, xOpen);
    StructLayoutAppend(sqlite3_module, xClose);
    StructLayoutAppend(sqlite3_module, xFilter);
    StructLayoutAppend(sqlite3_module, xNext);
    StructLayoutAppend(sqlite3_module, xEof);
    StructLayoutAppend(sqlite3_module, xColumn);
    StructLayoutAppend(sqlite3_module, xRowid);
    StructLayoutAppend(sqlite3_module, xUpdate);
    StructLayoutAppend(sqlite3_module, xBegin);
    StructLayoutAppend(sqlite3_module, xSync);
    StructLayoutAppend(sqlite3_module, xCommit);
    StructLayoutAppend(sqlite3_module, xRollback);
    StructLayoutAppend(sqlite3_module, xFindFunction);
    StructLayoutAppend(sqlite3_module, xRename);
    StructLayoutAppend(sqlite3_module, xSavepoint);
    StructLayoutAppend(sqlite3_module, xRelease);
    StructLayoutAppend(sqlite3_module, xRollbackTo);
    StructLayoutAppend(sqlite3_module, xShadowName);
    StructLayoutAppend(sqlite3_module, xIntegrity);
    StructLayoutEnd(sqlite3_module);
}

/**
 * Returns the layout for `sqlite3_vtab`.
 */
static ksqlite_layout struct_layout_sqlite3_vtab(int* layoutSize) {
    StructLayoutBegin(3);
    StructLayoutAppend(sqlite3_vtab, pModule);
    StructLayoutAppend(sqlite3_vtab, nRef);
    StructLayoutAppend(sqlite3_vtab, zErrMsg);
    StructLayoutEnd(sqlite3_vtab);
}

/**
 * Returns the layout for `sqlite3_vtab_cursor`.
 */
static ksqlite_layout struct_layout_sqlite3_vtab_cursor(int* layoutSize) {
    StructLayoutBegin(1);
    StructLayoutAppend(sqlite3_vtab_cursor, pVtab);
    StructLayoutEnd(sqlite3_vtab_cursor);
}

/**
 * Returns the layout for `sqlite3_file`.
 */
static ksqlite_layout struct_layout_sqlite3_file(int* layoutSize) {
    StructLayoutBegin(1);
    StructLayoutAppend(sqlite3_file, pMethods);
    StructLayoutEnd(sqlite3_file);
}

/**
 * Returns the layout for `sqlite3_io_methods`.
 */
static ksqlite_layout struct_layout_sqlite3_io_methods(int* layoutSize) {
    StructLayoutBegin(19);
    StructLayoutAppend(sqlite3_io_methods, iVersion);
    StructLayoutAppend(sqlite3_io_methods, xClose);
    StructLayoutAppend(sqlite3_io_methods, xRead);
    StructLayoutAppend(sqlite3_io_methods, xWrite);
    StructLayoutAppend(sqlite3_io_methods, xTruncate);
    StructLayoutAppend(sqlite3_io_methods, xSync);
    StructLayoutAppend(sqlite3_io_methods, xFileSize);
    StructLayoutAppend(sqlite3_io_methods, xLock);
    StructLayoutAppend(sqlite3_io_methods, xUnlock);
    StructLayoutAppend(sqlite3_io_methods, xCheckReservedLock);
    StructLayoutAppend(sqlite3_io_methods, xFileControl);
    StructLayoutAppend(sqlite3_io_methods, xSectorSize);
    StructLayoutAppend(sqlite3_io_methods, xDeviceCharacteristics);
    StructLayoutAppend(sqlite3_io_methods, xShmMap);
    StructLayoutAppend(sqlite3_io_methods, xShmLock);
    StructLayoutAppend(sqlite3_io_methods, xShmBarrier);
    StructLayoutAppend(sqlite3_io_methods, xShmUnmap);
    StructLayoutAppend(sqlite3_io_methods, xFetch);
    StructLayoutAppend(sqlite3_io_methods, xUnfetch);
    StructLayoutEnd(sqlite3_io_methods);
}

/**
 * Returns the layout for `sqlite3_vfs`.
 */
static ksqlite_layout struct_layout_sqlite3_vfs(int* layoutSize) {
    StructLayoutBegin(22);
    StructLayoutAppend(sqlite3_vfs, iVersion);
    StructLayoutAppend(sqlite3_vfs, szOsFile);
    StructLayoutAppend(sqlite3_vfs, mxPathname);
    StructLayoutAppend(sqlite3_vfs, pNext);
    StructLayoutAppend(sqlite3_vfs, zName);
    StructLayoutAppend(sqlite3_vfs, pAppData);
    StructLayoutAppend(sqlite3_vfs, xOpen);
    StructLayoutAppend(sqlite3_vfs, xDelete);
    StructLayoutAppend(sqlite3_vfs, xAccess);
    StructLayoutAppend(sqlite3_vfs, xFullPathname);
    StructLayoutAppend(sqlite3_vfs, xDlOpen);
    StructLayoutAppend(sqlite3_vfs, xDlError);
    StructLayoutAppend(sqlite3_vfs, xDlSym);
    StructLayoutAppend(sqlite3_vfs, xDlClose);
    StructLayoutAppend(sqlite3_vfs, xRandomness);
    StructLayoutAppend(sqlite3_vfs, xSleep);
    StructLayoutAppend(sqlite3_vfs, xCurrentTime);
    StructLayoutAppend(sqlite3_vfs, xGetLastError);
    StructLayoutAppend(sqlite3_vfs, xCurrentTimeInt64);
    StructLayoutAppend(sqlite3_vfs, xSetSystemCall);
    StructLayoutAppend(sqlite3_vfs, xGetSystemCall);
    StructLayoutAppend(sqlite3_vfs, xNextSystemCall);
    StructLayoutEnd(sqlite3_vfs);
}

/**
 * Returns the layout for `ksqlite_cipher_descriptor`.
 */
static ksqlite_layout struct_layout_ksqlite_cipher_descriptor(int* layoutSize) {
    StructLayoutBegin(11);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_name);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_allocateCipher);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_freeCipher);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_cloneCipher);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_getLegacy);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_getPageSize);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_getReserved);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_getSalt);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_generateKey);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_encryptPage);
    StructLayoutAppend(ksqlite_cipher_descriptor, m_decryptPage);
    StructLayoutEnd(ksqlite_cipher_descriptor);
}

/**
 * Returns the layout for `ksqlite_cipher_descriptor`.
 */
static ksqlite_layout struct_layout_ksqlite_cipher_params(int* layoutSize) {
    StructLayoutBegin(5);
    StructLayoutAppend(ksqlite_cipher_params, m_name);
    StructLayoutAppend(ksqlite_cipher_params, m_value);
    StructLayoutAppend(ksqlite_cipher_params, m_default);
    StructLayoutAppend(ksqlite_cipher_params, m_minValue);
    StructLayoutAppend(ksqlite_cipher_params, m_maxValue);
    StructLayoutEnd(ksqlite_cipher_params);
}

ksqlite_layout ksqlite_struct_layout_allocate(
    enum ksqlite_struct_type structType,
    int* layoutSize
) {
    switch (structType) {
        case Sqlite3IndexInfo:
            return struct_layout_sqlite3_index_info(layoutSize);
        case Sqlite3IndexConstraint:
            return struct_layout_sqlite3_index_constraint(layoutSize);
        case Sqlite3IndexConstraintUsage:
            return struct_layout_sqlite3_index_constraint_usage(layoutSize);
        case Sqlite3IndexOrderby:
            return struct_layout_sqlite3_index_order_by(layoutSize);
        case Sqlite3Module:
            return struct_layout_sqlite3_module(layoutSize);
        case Sqlite3Vtab:
            return struct_layout_sqlite3_vtab(layoutSize);
        case Sqlite3VtabCursor:
            return struct_layout_sqlite3_vtab_cursor(layoutSize);
        case Sqlite3File:
            return struct_layout_sqlite3_file(layoutSize);
        case Sqlite3IoMethods:
            return struct_layout_sqlite3_io_methods(layoutSize);
        case Sqlite3Vfs:
            return struct_layout_sqlite3_vfs(layoutSize);
        case KsqliteCipherDescriptor:
            return struct_layout_ksqlite_cipher_descriptor(layoutSize);
        case KsqliteCipherParams:
            return struct_layout_ksqlite_cipher_params(layoutSize);
        default:
            return 0;
    }
}

void ksqlite_struct_layout_free(ksqlite_layout layout) {
    sqlite3_free(layout);
}

#pragma clang diagnostic pop

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

int ksqlite_auto_extension(ksqlite_xEntryPoint callback) {
    return sqlite3_auto_extension((void (*)(void)) callback);
}

int ksqlite_cancel_auto_extension(ksqlite_xEntryPoint callback) {
    return sqlite3_cancel_auto_extension((void (*)(void)) callback);
}

int ksqlite_prepare_v2(
    sqlite3* db,
    const char* zSql,
    int nByte,
    sqlite3_stmt** ppStmt,
    int* const pzTailOffset
) {
    const char* zTail = 0;
    const int rc = sqlite3_prepare_v2(db, zSql, nByte, ppStmt, &zTail);

    if (pzTailOffset && zTail) {
        *pzTailOffset = (int) (zTail ? (zTail - zSql) : 0);
    }

    return rc;
}

int ksqlite_prepare_v3(
    sqlite3* db,
    const char* zSql,
    int nByte,
    unsigned int prepFlags,
    sqlite3_stmt** ppStmt,
    int* const pzTailOffset
) {
    const char* zTail = 0;
    const int rc = sqlite3_prepare_v3(db, zSql, nByte, prepFlags, ppStmt, &zTail);

    if (pzTailOffset && zTail) {
        *pzTailOffset = (int) (zTail ? (zTail - zSql) : 0);
    }

    return rc;
}

///////////////////////////////////////////////////////////////////////////
// Misc
///////////////////////////////////////////////////////////////////////////

#ifdef SQLITE_ENABLE_SQLLOG

// Requirement of SQLITE_ENABLE_SQLLOG
__attribute__((unused))
void sqlite3_init_sqllog(void) {
    // No logging by default, it is up to the application to set its own logging interceptor using
    // sqlite3_config
}

#endif