package ksqlite.capi.types

/**
 *These constants define the current transaction state of a database file. The
 * sqlite3_txn_state(D,S) interface returns one of these constants in order to describe the
 * transaction state of schema S in database connection D.
 *
 * [Allowed return values from sqlite3_txn_state()](https://sqlite.org/c3ref/c_txn_none.html)
 */
public enum class Sqlite3TransactionState(internal val value: Int) {

    /**
     * The SQLITE_TXN_NONE state means that no transaction is currently pending.
     */
    NONE(0),

    /**
     * The SQLITE_TXN_READ state means that the database is currently in a read transaction. Content
     * has been read from the database file but nothing in the database file has changed. The
     * transaction state will be advanced to SQLITE_TXN_WRITE if any changes occur and there are no
     * other conflicting concurrent write transactions. The transaction state will revert to
     * SQLITE_TXN_NONE following a ROLLBACK or COMMIT.
     */
    READ(1),

    /**
     * The SQLITE_TXN_WRITE state means that the database is currently in a write transaction.
     * Content has been written to the database file but has not yet committed. The transaction
     * state will change to SQLITE_TXN_NONE at the next ROLLBACK or COMMIT.
     */
    WRITE(2),
}