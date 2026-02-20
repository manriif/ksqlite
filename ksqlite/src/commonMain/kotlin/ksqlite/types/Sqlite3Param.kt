package ksqlite.types

import ksqlite.convertBooleanResult

///////////////////////////////////////////////////////////////////////////
// Int
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
 * Int parameter which can be written by SQLite on native side.
 * Unless specified, it is the owner responsibility to keep the object reachable.
 */
public class Sqlite3IntParam(initialValue: Int) : Sqlite3IntBaseParam(initialValue) {

    /**
     * The actual value of the parameter.
     */
    public val value: Int
        get() = intValue
}

/**
 * Parameter which can be written by SQLite on native side.
 * Unless specified, it is the owner responsibility to keep the object reachable.
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

///////////////////////////////////////////////////////////////////////////
// Long
///////////////////////////////////////////////////////////////////////////

/**
 * Long parameter which can be written by SQLite on native side.
 */
public expect class Sqlite3LongParam(initialValue: Long) {

    /**
     * The actual value of the parameter.
     */
    public val value: Long
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * String parameter which can be written by SQLite on native side.
 */
public expect class Sqlite3Utf8Param(initialValue: String?) {

    /**
     * The actual value of the parameter.
     */
    public val value: String?
}