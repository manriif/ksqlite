package ksqlite

public actual val sqliteLibVersion: String
    get() = sqlite.capi.sqlite3_libversion()