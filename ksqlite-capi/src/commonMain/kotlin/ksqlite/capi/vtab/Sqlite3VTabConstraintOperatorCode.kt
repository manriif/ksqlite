@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.vtab

/**
 * These macros define the allowed values for the sqlite3_index_info.aConstraint[].op field. Each
 * value represents an operator that is part of a constraint term in the WHERE clause of a query
 * that uses a virtual table.
 *
 * [Virtual Table Constraint Operator Codes](https://sqlite.org/c3ref/c_index_constraint_eq.html).
 */
public sealed class Sqlite3VTabConstraintOperatorCode(public open val code: Int) {

    public data object EQ : Sqlite3VTabConstraintOperatorCode(2)

    public data object GT : Sqlite3VTabConstraintOperatorCode(4)

    public data object LE : Sqlite3VTabConstraintOperatorCode(8)

    public data object LT : Sqlite3VTabConstraintOperatorCode(16)

    public data object GE : Sqlite3VTabConstraintOperatorCode(32)

    public data object MATCH : Sqlite3VTabConstraintOperatorCode(64)

    public data object LIKE : Sqlite3VTabConstraintOperatorCode(65)

    public data object GLOB : Sqlite3VTabConstraintOperatorCode(66)

    public data object REGEXP : Sqlite3VTabConstraintOperatorCode(67)

    public data object NE : Sqlite3VTabConstraintOperatorCode(68)

    public data object ISNOT : Sqlite3VTabConstraintOperatorCode(69)

    public data object ISNOTNULL : Sqlite3VTabConstraintOperatorCode(70)

    public data object ISNULL : Sqlite3VTabConstraintOperatorCode(71)

    public data object IS : Sqlite3VTabConstraintOperatorCode(72)

    public data object LIMIT : Sqlite3VTabConstraintOperatorCode(73)

    public data object OFFSET : Sqlite3VTabConstraintOperatorCode(74)

    public data object FUNCTION : Sqlite3VTabConstraintOperatorCode(150)

    /**
     * Custom operator code returned by xFindFunction callback.
     */
    public data class Custom(override val code: Int) : Sqlite3VTabConstraintOperatorCode(code) {
        init {
            require(code >= FUNCTION.code) {
                "Custom constraint code must be greater than or equals to ${FUNCTION.code}"
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Values
///////////////////////////////////////////////////////////////////////////

/**
 * Returns all [Sqlite3VTabConstraintOperatorCode]s except custom.
 */
internal fun sqlite3VTabConstraintOperatorCodes(): Set<Sqlite3VTabConstraintOperatorCode> = setOf(
    Sqlite3VTabConstraintOperatorCode.EQ,
    Sqlite3VTabConstraintOperatorCode.GT,
    Sqlite3VTabConstraintOperatorCode.LE,
    Sqlite3VTabConstraintOperatorCode.LT,
    Sqlite3VTabConstraintOperatorCode.GE,
    Sqlite3VTabConstraintOperatorCode.MATCH,
    Sqlite3VTabConstraintOperatorCode.LIKE,
    Sqlite3VTabConstraintOperatorCode.GLOB,
    Sqlite3VTabConstraintOperatorCode.REGEXP,
    Sqlite3VTabConstraintOperatorCode.NE,
    Sqlite3VTabConstraintOperatorCode.ISNOT,
    Sqlite3VTabConstraintOperatorCode.ISNOTNULL,
    Sqlite3VTabConstraintOperatorCode.ISNULL,
    Sqlite3VTabConstraintOperatorCode.IS,
    Sqlite3VTabConstraintOperatorCode.LIMIT,
    Sqlite3VTabConstraintOperatorCode.OFFSET,
    Sqlite3VTabConstraintOperatorCode.FUNCTION
)