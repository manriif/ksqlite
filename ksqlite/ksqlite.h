#include "ksqlite-generated.h"

/**
 * Callback expected by sqlite3_auto_extension() and sqlite3_cancel_auto_extension() with the
 * complete signature.
 */
typedef int (* xEntryPoint)(
    sqlite3*,
    const char**,
    const struct sqlite3_api_routines*
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