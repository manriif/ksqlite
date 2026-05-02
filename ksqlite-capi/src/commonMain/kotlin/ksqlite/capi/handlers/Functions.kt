package ksqlite.capi.handlers

import ksqlite.capi.types.Sqlite3TextEncoding

/**
 * Returns a unique name for a function handler given theses distinctive arguments.
 */
internal fun uniqueFunctionHandlerName(
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding
): String {
    return "$name$nArg${encoding.value}"
}