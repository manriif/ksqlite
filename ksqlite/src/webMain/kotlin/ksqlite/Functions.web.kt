package ksqlite

@Suppress("FunctionName")
public actual fun sqlite3_libversion(): String {
    return sqlite.capi.sqlite3_libversion()
}

actual fun sqlite3_aggregate_context(context: sqlite3_context, nBytes: Int) {
}