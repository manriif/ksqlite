#include "ksqlite.h"

int ksqlite_auto_extension(xEntryPoint callback) {
    return sqlite3_auto_extension((void (*)(void)) callback);
}

int ksqlite_cancel_auto_extension(xEntryPoint callback) {
    return sqlite3_cancel_auto_extension((void (*)(void)) callback);
}