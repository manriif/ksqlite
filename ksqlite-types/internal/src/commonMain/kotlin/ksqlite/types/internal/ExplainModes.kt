package ksqlite.types.internal

import ksqlite.types.SqliteExplainMode

/**
 * [SqliteExplainMode]s associated by their integer id.
 */
private val SqliteExplainModeMap = SqliteExplainMode.entries.associateBy(SqliteExplainMode::id)

/**
 * Converts [mode] to [SqliteExplainMode].
 */
public fun convertExplainMode(mode: Int): SqliteExplainMode {
    return checkNotNull(SqliteExplainModeMap[mode]) {
        "Unknown sqlite explain mode $mode"
    }
}