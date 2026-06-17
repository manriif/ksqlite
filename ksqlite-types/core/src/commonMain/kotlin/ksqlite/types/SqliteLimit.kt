@file:Suppress("SpellCheckingInspection")

package ksqlite.types

/**
 * These constants define various performance limits that can be lowered at run-time using
 * sqlite3_limit(). A concise description of these limits follows, and additional information is
 * available at Limits in SQLite.
 *
 * [Run-Time Limit Categories](https://sqlite.org/c3ref/c_limit_attached.html)
 */
public enum class SqliteLimit(public val id: Int) {

    /**
	 * The maximum size of any string or BLOB or table row, in bytes.
	 */
	LENGTH(0),

    /**
	 * The maximum length of an SQL statement, in bytes.
	 */
	SQL_LENGTH(1),

    /**
	 * The maximum number of columns in a table definition or in the result set of a SELECT or the
     * maximum number of columns in an index or in an ORDER BY or GROUP BY clause.
	 */
	COLUMN(2),

    /**
	 * The maximum depth of the parse tree on any expression.
	 */
	EXPR_DEPTH(3),

    /**
	 * The maximum number of terms in a compound SELECT statement.
	 */
	COMPOUND_SELECT(4),

    /**
	 * The maximum number of instructions in a virtual machine program used to implement an SQL
     * statement. If sqlite3_prepare_v2() or the equivalent tries to allocate space for more than
     * this many opcodes in a single prepared statement, an SQLITE_NOMEM error is returned.
	 */
	VDBE_OP(5),

    /**
	 * The maximum number of arguments on a function.
	 */
	FUNCTION_ARG(6),

    /**
	 * The maximum number of attached databases.
	 */
	ATTACHED(7),

    /**
	 * The maximum length of the pattern argument to the LIKE or GLOB operators.
	 */
	LIKE_PATTERN_LENGTH(8),

    /**
	 * The maximum index number of any parameter in an SQL statement.
	 */
	VARIABLE_NUMBER(9),

    /**
	 * The maximum depth of recursion for triggers.
	 */
	TRIGGER_DEPTH(10),

    /**
	 * The maximum number of auxiliary worker threads that a single prepared statement may start.
	 */
	WORKER_THREADS(11),

	/**
	 * The maximum depth of the LALR(1) parser stack used to analyze input SQL statements.
	 */
	PARSER_DEPTH(12)
}