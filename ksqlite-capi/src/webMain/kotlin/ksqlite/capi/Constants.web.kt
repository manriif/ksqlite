package ksqlite.capi

import kotlin.js.toJsBigInt

/**
 * Content will likely change in the near future and that SQLite should make its own private
 * copy of the content before returning.
 */
internal val SqliteTransient = (-1L).toJsBigInt()