package ksqlite.types.internal

import ksqlite.types.SqliteTransactionState

/**
 * [SqliteTransactionState]s associated by their integer id.
 */
private val SqliteTransactionStateMap =
    SqliteTransactionState.entries.associateBy(SqliteTransactionState::value)

/**
 * Converts [state] to [SqliteTransactionState].
 */
public fun convertTransactionState(state: Int): SqliteTransactionState {
    return checkNotNull(SqliteTransactionStateMap[state]) {
        "Unknown sqlite transaction state $state"
    }
}