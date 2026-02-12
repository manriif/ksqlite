package ksqlite

///////////////////////////////////////////////////////////////////////////
// Specials
///////////////////////////////////////////////////////////////////////////

/**
 * Content pointer is constant and will never change and does not need to be destroyed.
 */
public const val SQLITE_STATIC: Int = 0

/**
 * Content will likely change in the near future and that SQLite should make its own private
 * copy of the content before returning.
 */
public const val SQLITE_TRANSIENT: Int = -1