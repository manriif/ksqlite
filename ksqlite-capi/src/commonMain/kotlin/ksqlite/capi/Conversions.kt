package ksqlite.capi

import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3DataTypes
import ksqlite.capi.types.sqlite3Results
import ksqlite.capi.types.sqlite3TextEncodings

///////////////////////////////////////////////////////////////////////////
// Result
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3Result]s associated by their integer code.
 */
private val Sqlite3ResultMap = sqlite3Results().associateBy(Sqlite3Result::code)

/**
 * Converts [resultCode] to [Sqlite3Result].
 */
internal fun convertResult(resultCode: Int): Sqlite3Result {
    return checkNotNull(Sqlite3ResultMap[resultCode]) {
        "Unknown sqlite3 result code $resultCode"
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
 * Converts [dataType] to [Sqlite3DataType].
 */
internal fun convertDataType(dataType: Int): Sqlite3DataType {
    return checkNotNull(Sqlite3DataTypeMap[dataType]) {
        "Unknown sqlite3 data type $dataType"
    }
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
 * Converts [resultCode] to [Sqlite3CompleteResult].
 */
internal fun convertCompleteResult(resultCode: Int): Sqlite3CompleteResult = when (resultCode) {
    0 -> Sqlite3CompleteResult.Incomplete
    1 -> Sqlite3CompleteResult.Complete
    else -> Sqlite3CompleteResult.Failure(
        checkNotNull(convertResult(resultCode) as? Sqlite3Result.Failure)
    )
}