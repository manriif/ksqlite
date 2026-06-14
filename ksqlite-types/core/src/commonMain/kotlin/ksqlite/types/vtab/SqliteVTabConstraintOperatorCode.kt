@file:Suppress("SpellCheckingInspection")

package ksqlite.types.vtab

/**
 * These macros define the allowed values for the sqlite3_index_info.aConstraint[].op field. Each
 * value represents an operator that is part of a constraint term in the WHERE clause of a query
 * that uses a virtual table.
 *
 * [Virtual Table Constraint Operator Codes](https://sqlite.org/c3ref/c_index_constraint_eq.html).
 */
public sealed class SqliteVTabConstraintOperatorCode(public open val code: Int) {

    public data object EQ : SqliteVTabConstraintOperatorCode(2)

    public data object GT : SqliteVTabConstraintOperatorCode(4)

    public data object LE : SqliteVTabConstraintOperatorCode(8)

    public data object LT : SqliteVTabConstraintOperatorCode(16)

    public data object GE : SqliteVTabConstraintOperatorCode(32)

    public data object MATCH : SqliteVTabConstraintOperatorCode(64)

    public data object LIKE : SqliteVTabConstraintOperatorCode(65)

    public data object GLOB : SqliteVTabConstraintOperatorCode(66)

    public data object REGEXP : SqliteVTabConstraintOperatorCode(67)

    public data object NE : SqliteVTabConstraintOperatorCode(68)

    public data object ISNOT : SqliteVTabConstraintOperatorCode(69)

    public data object ISNOTNULL : SqliteVTabConstraintOperatorCode(70)

    public data object ISNULL : SqliteVTabConstraintOperatorCode(71)

    public data object IS : SqliteVTabConstraintOperatorCode(72)

    public data object LIMIT : SqliteVTabConstraintOperatorCode(73)

    public data object OFFSET : SqliteVTabConstraintOperatorCode(74)

    public data object FUNCTION : SqliteVTabConstraintOperatorCode(150)

    /**
     * Custom operator code returned by xFindFunction callback.
     */
    public data class Custom(override val code: Int) : SqliteVTabConstraintOperatorCode(code) {
        init {
            require(code >= FUNCTION.code) {
                "Custom constraint code must be greater than or equals to ${FUNCTION.code}"
            }
        }
    }
}