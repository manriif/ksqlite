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

__attribute__((unused))
const sqlite3_destructor_type KSQLITE_TRANSIENT = SQLITE_TRANSIENT;

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

#ifdef SQLITE_ENABLE_SQLLOG
// Requirement of SQLITE_ENABLE_SQLLOG
__attribute__((unused))
void sqlite3_init_sqllog(void) {
    // No logging by default, it is up to the application to set its own logging interceptor using
    // sqlite3_config
}
#endif