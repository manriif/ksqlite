package ksqlite.capi

import kotlin.js.toJsBigInt

///////////////////////////////////////////////////////////////////////////
// Magic addresses
///////////////////////////////////////////////////////////////////////////

/**
 * Content pointer is constant and will never change and does not need to be destroyed.
 */
internal val SqliteStatic = (0L).toJsBigInt()

/**
 * Content will likely change in the near future and that SQLite should make its own private
 * copy of the content before returning.
 */
internal val SqliteTransient = (-1L).toJsBigInt()