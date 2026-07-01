package ksqlite.types.internal

import ksqlite.types.SqliteConflictResolutionMode

/**
 * [SqliteConflictResolutionMode]s associated by their integer mode.
 */
private val SqliteConflictResolutionModeMap =
    SqliteConflictResolutionMode.entries.associateBy(SqliteConflictResolutionMode::mode)

/**
 * Converts [mode] to [SqliteConflictResolutionMode].
 */
public fun convertConflictResolutionMode(mode: Int): SqliteConflictResolutionMode =
    checkNotNull(SqliteConflictResolutionModeMap[mode]) {
        "Unknown SQLite conflict resolution mode $mode"
    }