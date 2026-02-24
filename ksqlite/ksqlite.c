#include "ksqlite.h"

int ksqlite3_auto_extension(
    int (* xEntryPoint)(
        sqlite3*,
        const char**,
        const struct sqlite3_api_routines*
    )
) {
    return sqlite3_auto_extension((void (*)(void)) xEntryPoint);
}