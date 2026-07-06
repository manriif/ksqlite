package ksqlite.types.internal

import ksqlite.types.SqliteExplainMode

/**
 * [SqliteExplainMode]s associated by their integer id.
 */
private val SqliteExplainModeMap = SqliteExplainMode.entries.associateBy(SqliteExplainMode::id)

/**
 * Converts [mode] to [SqliteExplainMode].
 */
public fun convertExplainMode(mode: Int): SqliteExplainMode =
    checkNotNull(SqliteExplainModeMap[mode]) { "Unknown SQLite explain mode: $mode" }