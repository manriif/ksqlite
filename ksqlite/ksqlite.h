#ifndef KSQLITE_H
#define KSQLITE_H

#ifndef __WASM__

#include "sqlite3mc_amalgamation.h"

#endif

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Callback expected by sqlite3_auto_extension() and sqlite3_cancel_auto_extension() with the
 * complete signature.
 */
typedef int (* xEntryPoint)(
    sqlite3* db,
    char** pzErrMsg,
    const struct sqlite3_api_routines* pThunk
);

/**
 * Wrappers function around sqlite3_auto_extension() with accept the xEntryPoint parameter with the
 * signature expected by SQLite. This is necessary for interop tools to generate compatible
 * code.
 *
 * @param xEntryPoint the XEntryPoint with expected signature.
 * @return sqlite3_auto_extension() result
 */
int ksqlite_auto_extension(xEntryPoint);

/**
 * Wrappers function around sqlite3_cancel_auto_extension() with accept the xEntryPoint parameter
 * with the signature expected by SQLite. This is necessary for interop tools to generate
 * compatible  code.
 *
 * @param xEntryPoint the XEntryPoint with expected signature.
 * @return sqlite3_cancel_auto_extension() result
 */
int ksqlite_cancel_auto_extension(xEntryPoint);

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