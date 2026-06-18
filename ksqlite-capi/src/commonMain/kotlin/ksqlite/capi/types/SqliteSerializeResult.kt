package ksqlite.capi.types

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.ReadableBuffer

/**
 * Result for [ksqlite.capi.sqlite3_serialize].
 */
public sealed interface SqliteSerializeResult {

    /**
     * Size of the database.
     */
    public val databaseSize: Long

    /**
     * SQLite returned a `null` buffer but may have supplied the [databaseSize].
     */
    public class Failure(override val databaseSize: Long) : SqliteSerializeResult

    /**
     * SQLite owns the [buffer].
     */
    public class Immutable(public val buffer: ReadableBuffer) : SqliteSerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }

    /**
     * The application is responsible for freeing the [buffer].
     */
    public class Mutable(public val buffer: Buffer) : SqliteSerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }
}