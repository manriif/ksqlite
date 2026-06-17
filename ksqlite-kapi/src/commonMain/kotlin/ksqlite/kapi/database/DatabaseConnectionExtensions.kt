package ksqlite.kapi.database

import kotlin.time.Duration

/**
 * Sets a [BusyHandler] that sleeps for a specified amount of time when a table is locked.
 * Any [BusyHandler] previously passed to [setBusyHandler] is replaced.
 *
 * The [duration] is coerced to [Int.MAX_VALUE] milliseconds.
 *
 * @throws ksqlite.kapi.SQLiteException if setting the timeout fails.
 */
public fun DatabaseConnection.setBusyTimeout(duration: Duration): Unit =
    setBusyTimeout(duration.inWholeMilliseconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())