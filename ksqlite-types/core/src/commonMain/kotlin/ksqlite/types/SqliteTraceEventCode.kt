/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ksqlite.types

/**
 * These constants identify classes of events that can be monitored using the sqlite3_trace_v2()
 * tracing logic. The M argument to sqlite3_trace_v2(D,M,X,P) is an OR-ed combination of one or more
 * of the following constants. The first argument to the trace callback is one of the following
 * constants.
 *
 * [SQL Trace Event Codes](https://sqlite.org/c3ref/c_trace.html).
 */
public sealed class SqliteTraceEventCode(public open val value: Int) {

    /**
     * Flag that is a constant.
     */
    public sealed class Constant(value: Int) : SqliteTraceEventCode(value)

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

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the trace API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteTraceEventCode(value) {

        override fun contains(flag: SqliteTraceEventCode): Boolean =
            (value and flag.value) == flag.value
    }

    /**
     * Returns an [SqliteTraceEventCode] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteTraceEventCode): SqliteTraceEventCode =
        Mask(value or flag.value)

    /**
     * Returns an [SqliteTraceEventCode] which is ANDed with [flag].
     */
    public infix fun and(flag: SqliteTraceEventCode): SqliteTraceEventCode =
        Mask(value and flag.value)

    /**
     * Returns an [SqliteTraceEventCode] which has [flag] removed.
     */
    public infix fun without(flag: SqliteTraceEventCode): SqliteTraceEventCode =
        Mask(value and flag.value.inv())

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: SqliteTraceEventCode): Boolean =
        flag == this || flag.value == value
}