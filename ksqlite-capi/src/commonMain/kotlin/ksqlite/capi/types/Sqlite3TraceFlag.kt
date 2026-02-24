@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.types

/**
 * These constants identify classes of events that can be monitored using the sqlite3_trace_v2()
 * tracing logic. The M argument to sqlite3_trace_v2(D,M,X,P) is an OR-ed combination of one or more
 * of the following constants. The first argument to the trace callback is one of the following
 * constants.
 *
 * [SQL Trace Event Codes](https://sqlite.org/c3ref/c_trace.html).
 */
public sealed class Sqlite3TraceFlag(internal open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : Sqlite3TraceFlag(value)

    /**
     * An SQLITE_TRACE_STMT callback is invoked when a prepared statement first begins running and
     * possibly at other times during the execution of the prepared statement, such as at the start
     * of each trigger subprogram. The P argument is a pointer to the prepared statement. The X
     * argument is a pointer to a string which is the unexpanded SQL text of the prepared statement
     * or an SQL comment that indicates the invocation of a trigger. The callback can compute the
     * same text that would have been returned by the legacy sqlite3_trace() interface by using the
     * X argument when X begins with "--" and invoking sqlite3_expanded_sql(P) otherwise.
     */
    public data object STMT : Constant(0x01)

    /**
     * An SQLITE_TRACE_PROFILE callback provides approximately the same information as is provided
     * by the sqlite3_profile() callback. The P argument is a pointer to the prepared statement and
     * the X argument points to a 64-bit integer which is approximately the number of nanoseconds
     * that the prepared statement took to run. The SQLITE_TRACE_PROFILE callback is invoked when
     * the statement finishes.
     */
    public data object PROFILE : Constant(0x02)

    /**
     * An SQLITE_TRACE_ROW callback is invoked whenever a prepared statement generates a single row
     * of result. The P argument is a pointer to the prepared statement and the X argument is
     * unused.
     */
    public data object ROW : Constant(0x04)

    /**
     * An SQLITE_TRACE_CLOSE callback is invoked when a database connection closes. The P argument
     * is a pointer to the database connection object and the X argument is unused.
     */
    public data object CLOSE : Constant(0x08)

    /**
     * Holder for the flags to be passed to the trace API function.
     */
    @ConsistentCopyVisibility
    public data class Masked internal constructor(override val value: Int) :
        Sqlite3TraceFlag(value)

    /**
     * Returns an [Sqlite3TraceFlag] which is ORed with [flag].
     */
    public infix fun or(flag: Sqlite3TraceFlag): Sqlite3TraceFlag {
        return Masked(value or flag.value)
    }
}