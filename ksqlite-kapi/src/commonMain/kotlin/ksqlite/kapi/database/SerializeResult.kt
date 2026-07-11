package ksqlite.kapi.database

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.ReadableBuffer

/**
 * Result for [DatabaseConnection.serialize].
 */
public sealed interface SerializeResult {

    /**
     * Size of the database.
     */
    public val databaseSize: Long

    /**
     * SQLite returned a `null` buffer but may have supplied the [databaseSize].
     */
    public class Failure(override val databaseSize: Long) : SerializeResult

    /**
     * SQLite owns the [buffer].
     */
    public class Immutable(public val buffer: ReadableBuffer) : SerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }

    /**
     * The application is responsible for freeing the [buffer].
     */
    public class Mutable(public val buffer: Buffer) : SerializeResult {

        override val databaseSize: Long
            get() = buffer.byteSize
    }
}