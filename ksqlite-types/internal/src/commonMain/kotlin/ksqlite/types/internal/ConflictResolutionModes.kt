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
public fun convertConflictResolutionMode(mode: Int): SqliteConflictResolutionMode {
    return checkNotNull(SqliteConflictResolutionModeMap[mode]) {
        "Unknown sqlite conflict resolution mode $mode"
    }
}