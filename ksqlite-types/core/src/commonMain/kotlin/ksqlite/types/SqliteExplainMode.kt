package ksqlite.types

/**
 * Available modes for prepared statement explain setting.
 *
 * [Change The EXPLAIN Setting For A Prepared Statement](https://sqlite.org/c3ref/stmt_explain.html)
 */
public enum class SqliteExplainMode(public val id: Int) {

    /**
	 * Statement becomes a normal prepared statement.
	 */
	NORMAL(0),

    /**
	 * Statement behaves as if its SQL text began with "EXPLAIN".
	 */
	EXPLAIN(1),

    /**
	 * Statement behaves as if its SQL text began with "EXPLAIN QUERY PLAN".
	 */
	EXPLAIN_QUERY_PLAN(2),
}