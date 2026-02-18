package ksqlite

import ksqlite.types.Sqlite3CompleteResult
import ksqlite.types.Sqlite3Result

///////////////////////////////////////////////////////////////////////////
// Result
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3Result]s associated by their integer code.
 */
internal val Sqlite3ResultMap = listOf(
    Sqlite3Result.ABORT,
    Sqlite3Result.ABORT.ROLLBACK,
    Sqlite3Result.AUTH,
    Sqlite3Result.AUTH.USER,
    Sqlite3Result.BUSY,
    Sqlite3Result.BUSY.RECOVERY,
    Sqlite3Result.BUSY.SNAPSHOT,
    Sqlite3Result.BUSY.TIMEOUT,
    Sqlite3Result.CANTOPEN,
    Sqlite3Result.CANTOPEN.CONVPATH,
    Sqlite3Result.CANTOPEN.DIRTYWAL,
    Sqlite3Result.CANTOPEN.FULLPATH,
    Sqlite3Result.CANTOPEN.ISDIR,
    Sqlite3Result.CANTOPEN.NOTEMPDIR,
    Sqlite3Result.CANTOPEN.SYMLINK,
    Sqlite3Result.CONSTRAINT,
    Sqlite3Result.CONSTRAINT.CHECK,
    Sqlite3Result.CONSTRAINT.COMMITHOOK,
    Sqlite3Result.CONSTRAINT.DATATYPE,
    Sqlite3Result.CONSTRAINT.FOREIGNKEY,
    Sqlite3Result.CONSTRAINT.FUNCTION,
    Sqlite3Result.CONSTRAINT.NOTNULL,
    Sqlite3Result.CONSTRAINT.PINNED,
    Sqlite3Result.CONSTRAINT.PRIMARYKEY,
    Sqlite3Result.CONSTRAINT.ROWID,
    Sqlite3Result.CONSTRAINT.TRIGGER,
    Sqlite3Result.CONSTRAINT.UNIQUE,
    Sqlite3Result.CONSTRAINT.VTAB,
    Sqlite3Result.CORRUPT,
    Sqlite3Result.CORRUPT.INDEX,
    Sqlite3Result.CORRUPT.SEQUENCE,
    Sqlite3Result.CORRUPT.VTAB,
    Sqlite3Result.DONE,
    Sqlite3Result.EMPTY,
    Sqlite3Result.ERROR,
    Sqlite3Result.ERROR.MISSING_COLLSEQ,
    Sqlite3Result.ERROR.RETRY,
    Sqlite3Result.ERROR.SNAPSHOT,
    Sqlite3Result.FORMAT,
    Sqlite3Result.FULL,
    Sqlite3Result.INTERNAL,
    Sqlite3Result.INTERRUPT,
    Sqlite3Result.IOERR,
    Sqlite3Result.IOERR.ACCESS,
    Sqlite3Result.IOERR.AUTH,
    Sqlite3Result.IOERR.BEGIN_ATOMIC,
    Sqlite3Result.IOERR.BLOCKED,
    Sqlite3Result.IOERR.CHECKRESERVEDLOCK,
    Sqlite3Result.IOERR.CLOSE,
    Sqlite3Result.IOERR.COMMIT_ATOMIC,
    Sqlite3Result.IOERR.CONVPATH,
    Sqlite3Result.IOERR.CORRUPTFS,
    Sqlite3Result.IOERR.DATA,
    Sqlite3Result.IOERR.DELETE,
    Sqlite3Result.IOERR.DELETE_NOENT,
    Sqlite3Result.IOERR.DIR_CLOSE,
    Sqlite3Result.IOERR.DIR_FSYNC,
    Sqlite3Result.IOERR.FSTAT,
    Sqlite3Result.IOERR.FSYNC,
    Sqlite3Result.IOERR.GETTEMPPATH,
    Sqlite3Result.IOERR.LOCK,
    Sqlite3Result.IOERR.MMAP,
    Sqlite3Result.IOERR.NOMEM,
    Sqlite3Result.IOERR.RDLOCK,
    Sqlite3Result.IOERR.READ,
    Sqlite3Result.IOERR.ROLLBACK_ATOMIC,
    Sqlite3Result.IOERR.SEEK,
    Sqlite3Result.IOERR.SHMLOCK,
    Sqlite3Result.IOERR.SHMMAP,
    Sqlite3Result.IOERR.SHMOPEN,
    Sqlite3Result.IOERR.SHMSIZE,
    Sqlite3Result.IOERR.SHORT_READ,
    Sqlite3Result.IOERR.TRUNCATE,
    Sqlite3Result.IOERR.UNLOCK,
    Sqlite3Result.IOERR.VNODE,
    Sqlite3Result.IOERR.WRITE,
    Sqlite3Result.LOCKED,
    Sqlite3Result.LOCKED.SHAREDCACHE,
    Sqlite3Result.LOCKED.VTAB,
    Sqlite3Result.MISMATCH,
    Sqlite3Result.MISUSE,
    Sqlite3Result.NOLFS,
    Sqlite3Result.NOMEM,
    Sqlite3Result.NOTADB,
    Sqlite3Result.NOTFOUND,
    Sqlite3Result.NOTICE,
    Sqlite3Result.NOTICE.RECOVER_ROLLBACK,
    Sqlite3Result.NOTICE.RECOVER_WAL,
    Sqlite3Result.OK,
    Sqlite3Result.OK.LOAD_PERMANENTLY,
    Sqlite3Result.PERM,
    Sqlite3Result.PROTOCOL,
    Sqlite3Result.RANGE,
    Sqlite3Result.READONLY,
    Sqlite3Result.READONLY.CANTINIT,
    Sqlite3Result.READONLY.CANTLOCK,
    Sqlite3Result.READONLY.DBMOVED,
    Sqlite3Result.READONLY.DIRECTORY,
    Sqlite3Result.READONLY.RECOVERY,
    Sqlite3Result.READONLY.ROLLBACK,
    Sqlite3Result.ROW,
    Sqlite3Result.SCHEMA,
    Sqlite3Result.TOOBIG,
    Sqlite3Result.WARNING,
    Sqlite3Result.WARNING.AUTOINDEX,
).associateBy(Sqlite3Result::code)

/**
 * Converts [result] to [Sqlite3Result].
 */
internal fun convertResult(result: Int): Sqlite3Result {
    return checkNotNull(Sqlite3ResultMap[result]) {
        "Unknown sqlite3 result code $result"
    }
}

///////////////////////////////////////////////////////////////////////////
// Boolean
///////////////////////////////////////////////////////////////////////////

/**
 * Converts [result] to [Boolean].
 */
internal fun convertBooleanResult(result: Int): Boolean = when (result) {
    0 -> false
    1 -> true
    else -> error("Unexpected integer result $result for boolean conversion")
}

///////////////////////////////////////////////////////////////////////////
// Complete
///////////////////////////////////////////////////////////////////////////

/**
 * Converts [result] to [Sqlite3CompleteResult].
 */
internal fun convertCompleteResult(result: Int): Sqlite3CompleteResult = when (result) {
    0 -> Sqlite3CompleteResult.Incomplete
    1 -> Sqlite3CompleteResult.Complete
    else -> Sqlite3CompleteResult.Failure(checkNotNull(convertResult(result) as? Sqlite3Result.Failure))
}