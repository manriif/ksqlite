package ksqlite

import org.sqlite.jni.capi.CApi

public actual val sqliteLibVersion: String
    get() = CApi.sqlite3_libversion()