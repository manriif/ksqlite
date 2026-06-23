#ifndef __WASM__
#include "ksqlite.h"
#endif

#ifdef SQLITE_ENABLE_SQLLOG
// Requirement of SQLITE_ENABLE_SQLLOG
__attribute__((unused))
void sqlite3_init_sqllog(void) {
    // No logging by default, it is up to the application to set its own logging interceptor using
    // sqlite3_config
}
#endif

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
