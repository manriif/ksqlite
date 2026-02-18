package ksqlite.types

/**
 * Primary result codes.
 *
 * [Result and Error Codes](https://sqlite.org/rescode.html)
 */
public enum class Sqlite3PrimaryResultCode(public val raw: Int) {
    /**
     * The SQLITE_ABORT result code indicates that an operation was aborted prior to completion,
     * usually by application request. See also: SQLITE_INTERRUPT.
     *
     * If the callback function to sqlite3_exec() returns non-zero, then sqlite3_exec() will return
     * SQLITE_ABORT.
     *
     * If a ROLLBACK operation occurs on the same
     * [database connection](https://sqlite.org/c3ref/sqlite3.html) as a pending read or write,
     * then the pending read or write may fail with an SQLITE_ABORT or SQLITE_ABORT_ROLLBACK error.
     *
     * In addition to being a result code, the SQLITE_ABORT value is also used as a conflict
     * resolution mode returned from the sqlite3_vtab_on_conflict() interface.
     */
    ABORT(4),

    /**
     * The SQLITE_AUTH error is returned when the authorizer callback indicates that an SQL
     * statement being prepared is not authorized.
     */
    AUTH(23),

    /**
     * The SQLITE_BUSY result code indicates that the database file could not be written (or in some
     * cases read) because of concurrent activity by some other
     * [database connection](https://sqlite.org/c3ref/sqlite3.html), usually a
     * [database connection](https://sqlite.org/c3ref/sqlite3.html) in a separate process.
     *
     * For example, if process A is in the middle of a large write transaction and at the same time
     * process B attempts to start a new write transaction, process B will get back an SQLITE_BUSY
     * result because SQLite only supports one writer at a time. Process B will need to wait for
     * process A to finish its transaction before starting a new transaction. The
     * sqlite3_busy_timeout() and sqlite3_busy_handler() interfaces and the busy_timeout pragma
     * are available to process B to help it deal with SQLITE_BUSY errors.
     *
     * An SQLITE_BUSY error can occur at any point in a transaction: when the transaction is first
     * started, during any write or update operations, or when the transaction commits. To avoid
     * encountering SQLITE_BUSY errors in the middle of a transaction, the application can use BEGIN
     * IMMEDIATE instead of just BEGIN to start a transaction. The BEGIN IMMEDIATE command might
     * itself return SQLITE_BUSY, but if it succeeds, then SQLite guarantees that no subsequent
     * operations on the same database through the next COMMIT will return SQLITE_BUSY.
     *
     * See also: SQLITE_BUSY_RECOVERY and SQLITE_BUSY_SNAPSHOT.
     *
     * The SQLITE_BUSY result code differs from SQLITE_LOCKED in that SQLITE_BUSY indicates a
     * conflict with a separate [database connection](https://sqlite.org/c3ref/sqlite3.html),
     * probably in a separate process, whereas SQLITE_LOCKED indicates a conflict within the same
     * [database connection](https://sqlite.org/c3ref/sqlite3.html) (or sometimes a
     * [database connection](https://sqlite.org/c3ref/sqlite3.html) with a
     * [shared cache](https://sqlite.org/sharedcache.html)).
     */
    BUSY(5),

    /**
     * The SQLITE_ERROR result code is a generic error code that is used when no other more specific
     * error code is available.
     */
    ERROR(1),

    /**
     * The SQLITE_LOCKED result code indicates that a write operation could not continue because of
     * a conflict within the same [database connection](https://sqlite.org/c3ref/sqlite3.html) or a
     * conflict with a different database
     * connection that uses a [shared cache](https://sqlite.org/sharedcache.html).
     *
     * For example, a DROP TABLE statement cannot be run while another thread is reading from that
     * table on the same [database connection](https://sqlite.org/c3ref/sqlite3.html) because
     * dropping the table would delete the table out from under the concurrent reader.
     *
     * The SQLITE_LOCKED result code differs from SQLITE_BUSY in that SQLITE_LOCKED indicates a
     * conflict on the same [database connection](https://sqlite.org/c3ref/sqlite3.html) (or on a
     * connection with a [shared cache](https://sqlite.org/sharedcache.html)) whereas SQLITE_BUSY
     * indicates a conflict with a different
     * [database connection](https://sqlite.org/c3ref/sqlite3.html), probably in a different
     * process.
     */
    LOCKED(6),

    /**
     * The SQLITE_NOMEM result code indicates that SQLite was unable to allocate all the memory it
     * needed to complete the operation. In other words, an internal call to sqlite3_malloc() or
     * sqlite3_realloc() has failed in a case where the memory being allocated was required in order
     * to continue the operation.
     */
    NOMEM(7),

    /**
     * The SQLITE_READONLY result code is returned when an attempt is made to alter some data for
     * which the current [database connection](https://sqlite.org/c3ref/sqlite3.html) does not have
     * write permission.
     */
    READ_ONLY(8),

    /**
     * The SQLITE_INTERRUPT result code indicates that an operation was interrupted by the
     * sqlite3_interrupt() interface. See also: SQLITE_ABORT.
     */
    INTERRUPT(9),

    /**
     * The SQLITE_IOERR result code says that the operation could not finish because the operating
     * system reported an I/O error.
     *
     * A full disk drive will normally give an SQLITE_FULL error rather than an SQLITE_IOERR error.
     *
     * There are many different extended result codes for I/O errors that identify the specific
     * I/O operation that failed.
     */
    IOERR(10),

    /**
     * The SQLITE_CORRUPT result code indicates that the database file has been corrupted. See the
     * [How To Corrupt Your Database Files](https://sqlite.org/lockingv3.html#how_to_corrupt) for
     * further discussion on how corruption can occur.
     */
    CORRUPT(11),

    /**
     * The SQLITE_NOTFOUND result code is exposed in three ways:
     *
     * 1. SQLITE_NOTFOUND can be returned by the sqlite3_file_control() interface to indicate that
     * the [file control opcode](https://sqlite.org/c3ref/c_fcntl_begin_atomic_write.html) passed as the
     * third argument was not recognized by the underlying VFS.
     *
     * 2. SQLITE_NOTFOUND can also be returned by the xSetSystemCall() method of an sqlite3_vfs
     * object.
     *
     * 3. SQLITE_NOTFOUND can be returned by sqlite3_vtab_rhs_value() to indicate that the
     * right-hand operand of a constraint is not available to the xBestIndex method that made the
     * call.
     *
     * The SQLITE_NOTFOUND result code is also used internally by the SQLite implementation, but
     * those internal uses are not exposed to the application.
     */
    NOTFOUND(12),

    /**
     * The SQLITE_FULL result code indicates that a write could not complete because the disk is
     * full. Note that this error can occur when trying to write information into the main database
     * file, or it can also occur when writing into
     * [temporary disk files](https://sqlite.org/tempfiles.html).
     *
     * Sometimes applications encounter this error even though there is an abundance of primary
     * disk space because the error occurs when writing into
     * [temporary disk files](https://sqlite.org/tempfiles.html) on a system where temporary files
     * are stored on a separate partition with much less space than the primary disk.
     */
    FULL(13),

    /**
     * The SQLITE_CANTOPEN result code indicates that SQLite was unable to open a file. The file in
     * question might be a primary database file or one of several
     * [temporary disk files](https://sqlite.org/tempfiles.html).
     */
    CANTOPEN(14),

    /**
     * The SQLITE_PROTOCOL result code indicates a problem with the file locking protocol used by
     * SQLite. The SQLITE_PROTOCOL error is currently only returned when using
     * [WAL mode](https://sqlite.org/wal.html) and attempting to start a new transaction. There is
     * a race condition that can occur when two separate
     * [database connections](https://sqlite.org/c3ref/sqlite3.html) both try to start a transaction
     * at the same time in [WAL mode](https://sqlite.org/wal.html). The loser of the race backs off
     * and tries again, after a brief delay. If the same connection loses the locking race dozens of
     * times over a span of multiple seconds, it will eventually give up and return SQLITE_PROTOCOL.
     * The SQLITE_PROTOCOL error should appear in practice very, very rarely,
     * and only when there are many separate processes all competing intensely to write to the same
     * database.
     */
    PROTOCOL(15),

    /**
     * The SQLITE_EMPTY result code is not currently used.
     */
    EMPTY(16)
}