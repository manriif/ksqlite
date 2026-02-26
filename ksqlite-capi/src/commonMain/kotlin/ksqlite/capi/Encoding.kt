package ksqlite.capi

import ksqlite.capi.types.Sqlite3TextEncoding

/**
 * Throws if [E] does not contains UTF8 flag.
 */
internal fun <E : Sqlite3TextEncoding> E.utf8OrThrow(): E {
    if ((value and Sqlite3TextEncoding.UTF8.value) != Sqlite3TextEncoding.UTF8.value) {
        throw UnsupportedOperationException("Encoding other than UTF8 are not supported")
    }

    return this
}