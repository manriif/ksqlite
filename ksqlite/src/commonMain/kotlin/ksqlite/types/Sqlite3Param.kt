package ksqlite.types

import ksqlite.convertBooleanResult

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

public expect abstract class PointerType

/**
 * Parameter of type [Pointer], intended to be allocated by SQLite.
 */
public expect class Sqlite3PointerParam<Pointer : PointerType>() {

    /**
     * The pointer to the struct value.
     * May be `null` if an error occurred while SQLite tried to allocate the struct.
     */
    public val value: Pointer?
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Parameter of type [String], encoded as UTF8, intended to be written by SQLite.
 * The owner can optionally supply an [initialValue].
 */
public expect class Sqlite3Utf8Param(initialValue: String?) {

    /**
     * Reads the actual value of the parameter and returns a new UTF8 encoded [String].
     */
    public fun readValue(): String?
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

/**
 * Int parameter which can be written by SQLite on native side.
 */
public expect open class Sqlite3IntBaseParam internal constructor(initialValue: Int) {

    /**
     * The actual value of the parameter.
     */
    internal open val intValue: Int
}

/**
 * Parameter of type [Int] intended to be written by SQLite.
 * The owner can optionally supply an [initialValue].
 */
public class Sqlite3IntParam(initialValue: Int) : Sqlite3IntBaseParam(initialValue) {

    /**
     * The actual value of the parameter.
     */
    public val value: Int
        get() = intValue
}

/**
 * Parameter of type [Int], interpreted as [Boolean], intended to be written by SQLite.
 * The owner can optionally supply an [initialValue].
 */
public class Sqlite3BooleanParam(initialValue: Boolean) : Sqlite3IntBaseParam(
    initialValue = if (initialValue) 1 else 0
) {

    /**
     * The actual value of the parameter.
     */
    public val value: Boolean
        get() = convertBooleanResult(intValue)
}

/**
 * Parameter of type [Long] intended to be written by SQLite.
 * The owner can optionally supply an [initialValue].
 */
public expect class Sqlite3LongParam(initialValue: Long) {

    /**
     * The actual value of the parameter.
     */
    public val value: Long
}