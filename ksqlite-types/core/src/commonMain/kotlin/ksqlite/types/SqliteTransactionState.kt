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
 *These constants define the current transaction state of a database file. The
 * sqlite3_txn_state(D,S) interface returns one of these constants in order to describe the
 * transaction state of schema S in database connection D.
 *
 * [Allowed return values from sqlite3_txn_state()](https://sqlite.org/c3ref/c_txn_none.html)
 */
public enum class SqliteTransactionState(public val value: Int) {

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