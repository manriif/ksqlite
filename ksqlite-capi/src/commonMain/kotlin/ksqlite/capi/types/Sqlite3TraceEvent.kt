@file:Suppress("ClassName")

package ksqlite.capi.types

/**
 * Event passed as parameter to [Sqlite3TraceCallback].
 */
public sealed interface Sqlite3TraceEvent {

    /**
     * The trace constant.
     */
    public val constant: Sqlite3TraceFlag.Constant

    /**
     * [Sqlite3TraceFlag.STMT] related event.
     */
    public class Stmt(
        override val constant: Sqlite3TraceFlag.Constant,
        public val stmt: Sqlite3RestrictedStruct<sqlite3_stmt>,
        public val sql: String
    ) : Sqlite3TraceEvent

    /**
     * [Sqlite3TraceFlag.PROFILE] related event.
     */
    public class Profile(
        override val constant: Sqlite3TraceFlag.Constant,
        public val stmt: Sqlite3RestrictedStruct<sqlite3_stmt>,
        public val nanos: Long
    ) : Sqlite3TraceEvent

    /**
     * [Sqlite3TraceFlag.ROW] related event.
     */
    public class Row(
        override val constant: Sqlite3TraceFlag.Constant,
        public val stmt: Sqlite3RestrictedStruct<sqlite3_stmt>,
    ) : Sqlite3TraceEvent

    /**
     * [Sqlite3TraceFlag.CLOSE] related event.
     */
    public class Close(
        override val constant: Sqlite3TraceFlag.Constant,
        public val db: Sqlite3RestrictedStruct<sqlite3>,
    ) : Sqlite3TraceEvent
}