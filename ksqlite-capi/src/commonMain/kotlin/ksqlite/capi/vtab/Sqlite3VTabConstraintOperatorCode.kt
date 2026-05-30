package ksqlite.capi.vtab

/**
 * These macros define the allowed values for the sqlite3_index_info.aConstraint[].op field. Each
 * value represents an operator that is part of a constraint term in the WHERE clause of a query
 * that uses a virtual table.
 *
 * [Virtual Table Constraint Operator Codes](https://sqlite.org/c3ref/c_index_constraint_eq.html).
 */
public enum class Sqlite3VTabConstraintOperatorCode(internal val code: Int) {
    EQ(2),
    GT(4),
    LE(8),
    LT(16),
    GE(32),
    MATCH(64),
    LIKE(65),
    GLOB(66),
    REGEXP(67),
    NE(68),
    ISNOT(69),
    ISNOTNULL(70),
    ISNULL(71),
    IS(72),
    LIMIT(73),
    OFFSET(74),
    FUNCTION(150);
}