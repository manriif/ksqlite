@file:Suppress("SpellCheckingInspection")

package ksqlite.types

/**
 * These constants may be ORed together with the preferred text encoding as the fourth argument to
 * sqlite3_create_function(), sqlite3_create_function16(), or sqlite3_create_function_v2().
 *
 * [Function Flags](https://sqlite.org/c3ref/c_deterministic.html).
 */
public enum class Sqlite3FunctionFlag(internal val value: Int) {

    /**
     * The SQLITE_DETERMINISTIC flag means that the new function always gives the same output when
     * the input parameters are the same. The abs() function is deterministic, for example, but
     * randomblob() is not. Functions must be deterministic in order to be used in certain contexts
     * such as with the WHERE clause of partial indexes or in generated columns. SQLite might also
     * optimize deterministic functions by factoring them out of inner loops.
     */
    DETERMINISTIC(0x000000800),

    /**
     * The SQLITE_DIRECTONLY flag means that the function may only be invoked from top-level SQL,
     * and cannot be used in VIEWs or TRIGGERs nor in schema structures such as CHECK constraints,
     * DEFAULT clauses, expression indexes, partial indexes, or generated columns.
     *
     * The SQLITE_DIRECTONLY flag is recommended for any application-defined SQL function that has
     * side-effects or that could potentially leak sensitive information. This will prevent attacks
     * in which an application is tricked into using a database file that has had its schema
     * surreptitiously modified to invoke the application-defined function in ways that are harmful.
     *
     * Some people say it is good practice to set SQLITE_DIRECTONLY on all application-defined SQL
     * functions, regardless of whether or not they are security sensitive, as doing so prevents
     * those functions from being used inside of the database schema, and thus ensures that the
     * database can be inspected and modified using generic tools (such as the CLI) that do not have
     * access to the application-defined functions.
     */
    DIRECTONLY(0x000080000),

    /**
     * The SQLITE_INNOCUOUS flag means that the function is unlikely to cause problems even if
     * misused. An innocuous function should have no side effects and should not depend on any
     * values other than its input parameters. The abs() function is an example of an innocuous
     * function. The load_extension() SQL function is not innocuous because of its side effects.
     * SQLITE_INNOCUOUS is similar to SQLITE_DETERMINISTIC, but is not exactly the same. The
     * random() function is an example of a function that is innocuous but not deterministic.
     *
     * Some heightened security settings (SQLITE_DBCONFIG_TRUSTED_SCHEMA and PRAGMA
     * trusted_schema=OFF) disable the use of SQL functions inside views and triggers and in schema
     * structures such as CHECK constraints, DEFAULT clauses, expression indexes, partial indexes,
     * and generated columns unless the function is tagged with SQLITE_INNOCUOUS. Most built-in
     * functions are innocuous. Developers are advised to avoid using the SQLITE_INNOCUOUS flag for
     * application-defined functions unless the function has been carefully audited and found to be
     * free of potentially security-adverse side-effects and information-leaks.
     */
    INNOCUOUS(0x000200000),

    /**
     * The SQLITE_RESULT_SUBTYPE flag indicates to SQLite that a function might call
     * sqlite3_result_subtype() to cause a sub-type to be associated with its result. Every function
     * that invokes sqlite3_result_subtype() should have this property. If it does not, then the
     * call to sqlite3_result_subtype() might become a no-op if the function is used as a term in an
     * expression index. On the other hand, SQL functions that never invoke sqlite3_result_subtype()
     * should avoid setting this property, as the purpose of this property is to disable certain
     * optimizations that are incompatible with subtypes.
     */
    RESULT_SUBTYPE(0x001000000),

    /**
     * The SQLITE_SELFORDER1 flag indicates that the function is an aggregate that internally orders
     * the values provided to the first argument. The ordered-set aggregate SQL notation with a
     * single ORDER BY term can be used to invoke this function. If the ordered-set aggregate
     * notation is used on a function that lacks this flag, then an error is raised. Note that the
     * ordered-set aggregate syntax is only available if SQLite is built using the
     * -DSQLITE_ENABLE_ORDERED_SET_AGGREGATES compile-time option.
     */
    SELFORDER1(0x002000000),

    /**
     * The SQLITE_SUBTYPE flag indicates to SQLite that a function might call
     * sqlite3_value_subtype() to inspect the sub-types of its arguments. This flag instructs SQLite
     * to omit some corner-case optimizations that might disrupt the operation of the
     * sqlite3_value_subtype() function, causing it to return zero rather than the correct
     * subtype(). All SQL functions that invoke sqlite3_value_subtype() should have this property.
     * If the SQLITE_SUBTYPE property is omitted, then the return value from sqlite3_value_subtype()
     * might sometimes be zero even though a non-zero subtype was specified by the function argument
     * expression.
     */
    SUBTYPE(0x000100000)
}