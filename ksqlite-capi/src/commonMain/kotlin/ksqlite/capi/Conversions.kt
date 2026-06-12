package ksqlite.capi

import ksqlite.capi.types.Sqlite3ActionCode
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConflictResolutionMode
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.sqlite3ActionCodes
import ksqlite.capi.types.sqlite3DataTypes
import ksqlite.capi.types.sqlite3Results
import ksqlite.capi.types.sqlite3TextEncodings
import ksqlite.capi.vtab.Sqlite3VTabConstraintOperatorCode
import ksqlite.capi.vtab.sqlite3VTabConstraintOperatorCodes

///////////////////////////////////////////////////////////////////////////
// Result
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3Result]s associated by their integer code.
 */
private val Sqlite3ResultMap = sqlite3Results().associateBy(Sqlite3Result::code)

/**
 * Converts [code] to [Sqlite3Result].
 */
internal fun convertResult(code: Int): Sqlite3Result {
    return checkNotNull(Sqlite3ResultMap[code]) {
        "Unknown sqlite3 result code $code"
    }
}

///////////////////////////////////////////////////////////////////////////
// Action codes
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3ActionCode]s associated by their integer code.
 */
private val Sqlite3ActionCodeMap = sqlite3ActionCodes().associateBy(Sqlite3ActionCode::code)

/**
 * Converts [code] to [Sqlite3ActionCode].
 */
internal inline fun <reified A : Sqlite3ActionCode> convertActionCode(code: Int): A {
    val actionCode = checkNotNull(Sqlite3ActionCodeMap[code]) {
        "Unknown sqlite3 action code $code"
    }

    check(actionCode is A) { "Unexpected action type $actionCode" }
    return actionCode
}

///////////////////////////////////////////////////////////////////////////
// Collation
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3TextEncoding]s associated by their integer value.
 */
private val Sqlite3TextEncodings = sqlite3TextEncodings()

/**
 * Converts [encoding] into [Sqlite3TextEncoding].
 */
internal inline fun <reified E : Sqlite3TextEncoding> convertTextEncoding(encoding: Int): E {
    val value = Sqlite3TextEncodings.firstOrNull { (encoding and it.value) == it.value }
    checkNotNull(value) { "Unknown sqlite3 text encoding $encoding" }
    check(value is E) { "Unexpected encoding type $value" }
    return value
}

///////////////////////////////////////////////////////////////////////////
// Complete result
///////////////////////////////////////////////////////////////////////////

/**
 * Converts [code] to [Sqlite3CompleteResult].
 */
internal fun convertCompleteResult(code: Int): Sqlite3CompleteResult = when (code) {
    0 -> Sqlite3CompleteResult.Incomplete
    1 -> Sqlite3CompleteResult.Complete
    else -> Sqlite3CompleteResult.Failure(
        checkNotNull(convertResult(code) as? Sqlite3Result.Failure)
    )
}

///////////////////////////////////////////////////////////////////////////
// Conflict resolution mode
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3ConflictResolutionMode]s associated by their integer mode.
 */
private val Sqlite3ConflictResolutionModeMap =
    Sqlite3ConflictResolutionMode.entries.associateBy(Sqlite3ConflictResolutionMode::code)

/**
 * Converts [code] to [Sqlite3ConflictResolutionMode].
 */
internal fun convertConflictResolutionMode(code: Int): Sqlite3ConflictResolutionMode {
    return checkNotNull(Sqlite3ConflictResolutionModeMap[code]) {
        "Unknown sqlite3 conflict resolution mode $code"
    }
}

///////////////////////////////////////////////////////////////////////////
// Data types
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3DataType]s associated by their integer code.
 */
private val Sqlite3DataTypeMap = sqlite3DataTypes().associateBy(Sqlite3DataType::code)

/**
 * Converts [type] to [Sqlite3DataType].
 */
internal fun convertDataType(type: Int): Sqlite3DataType {
    return checkNotNull(Sqlite3DataTypeMap[type]) {
        "Unknown sqlite3 data type $type"
    }
}

///////////////////////////////////////////////////////////////////////////
// Explain modes
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3ExplainMode]s associated by their integer id.
 */
private val Sqlite3ExplainModeMap = Sqlite3ExplainMode.entries.associateBy(Sqlite3ExplainMode::id)

/**
 * Converts [mode] to [Sqlite3ExplainMode].
 */
internal fun convertExplainMode(mode: Int): Sqlite3ExplainMode {
    return checkNotNull(Sqlite3ExplainModeMap[mode]) {
        "Unknown sqlite3 explain mode $mode"
    }
}

///////////////////////////////////////////////////////////////////////////
// Transaction state
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3TransactionState]s associated by their integer id.
 */
private val Sqlite3TransactionStateMap =
    Sqlite3TransactionState.entries.associateBy(Sqlite3TransactionState::value)

/**
 * Converts [state] to [Sqlite3TransactionState].
 */
internal fun convertTransactionState(state: Int): Sqlite3TransactionState {
    return checkNotNull(Sqlite3TransactionStateMap[state]) {
        "Unknown sqlite3 transaction state $state"
    }
}

///////////////////////////////////////////////////////////////////////////
// Virtual table constraint operator codes
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3VTabConstraintOperatorCode]s associated by their integer code.
 */
private val Sqlite3VTabConstraintOperatorCodeMap =
    sqlite3VTabConstraintOperatorCodes().associateBy(Sqlite3VTabConstraintOperatorCode::code)

/**
 * Converts [code] to [Sqlite3VTabConstraintOperatorCode].
 */
internal fun convertVTabConstraintOperatorCode(code: Int): Sqlite3VTabConstraintOperatorCode {
    return Sqlite3VTabConstraintOperatorCodeMap[code]
        ?: Sqlite3VTabConstraintOperatorCode.Custom(code)
}