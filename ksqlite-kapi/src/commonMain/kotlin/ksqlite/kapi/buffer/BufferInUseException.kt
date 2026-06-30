package ksqlite.kapi.buffer

/**
 * Exception raised when an attempt was made to modify a [Buffer] while SQLite was borrowing it.
 */
public class BufferInUseException(message: String) : RuntimeException(message)