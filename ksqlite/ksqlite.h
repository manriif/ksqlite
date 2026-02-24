#include "ksqlite-generated.h"

/**
 * Wrappers function around sqlite3_auto_extension() with accept the xEntryPoint parameter with the
 * real signature expected by SQLite. This is necessary for interop tools to generate compatible
 * code.
 *
 * @param xEntryPoint the XEntryPoint with real signature.
 * @return sqlite3_auto_extension() result
 */
int ksqlite_auto_extension(
    int (* xEntryPoint)(
        sqlite3*,
        const char**,
        const struct sqlite3_api_routines*
    )
);