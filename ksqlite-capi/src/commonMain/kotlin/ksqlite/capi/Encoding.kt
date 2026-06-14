package ksqlite.capi

import ksqlite.types.SqliteTextEncoding

/**
 * Throws if [E] does not contains UTF8 flag.
 */
internal fun <E : SqliteTextEncoding> E.utf8OrThrow(): E {
    if ((value and SqliteTextEncoding.UTF8.value) != SqliteTextEncoding.UTF8.value) {
        throw UnsupportedOperationException("Encoding other than UTF8 are not supported")
    }

    return this
}