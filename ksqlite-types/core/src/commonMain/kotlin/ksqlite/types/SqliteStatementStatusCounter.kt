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
@file:Suppress("SpellCheckingInspection")

package ksqlite.types

/**
 * These constants define integer codes that name counter values associated with the
 * sqlite3_stmt_status() interface.
 *
 * [Status Parameters for prepared statements](https://sqlite.org/c3ref/c_stmtstatus_counter.html)
 */
public enum class SqliteStatementStatusCounter(public val id: Int) {

    /**
     * This is the number of times that SQLite has stepped forward in a table as part of a full
     * table scan. Large numbers for this counter may indicate opportunities for performance
     * improvement through careful use of indices.
     */
    FULLSCAN_STEP(1),

    /**
     * This is the number of sort operations that have occurred. A non-zero value in this counter
     * may indicate an opportunity to improve performance through careful use of indices.
     */
    SORT(2),

    /**
     * This is the number of rows inserted into transient indices that were created automatically in
     * order to help joins run faster. A non-zero value in this counter may indicate an opportunity
     * to improve performance by adding permanent indices that do not need to be reinitialized each
     * time the statement is run.
     */
    AUTOINDEX(3),

    /**
     * This is the number of virtual machine operations executed by the prepared statement if that
     * number is less than or equal to 2147483647. The number of virtual machine operations can be
     * used as a proxy for the total work done by the prepared statement. If the number of virtual
     * machine operations exceeds 2147483647 then the value returned by this statement status code
     * is undefined.
     */
    VM_STEP(4),

    /**
     * This is the number of times that the prepare statement has been automatically regenerated due
     * to schema changes or changes to bound parameters that might affect the query plan.
     */
    REPREPARE(5),

    /**
     * This is the number of times that the prepared statement has been run. A single "run" for the
     * purposes of this counter is one or more calls to sqlite3_step() followed by a call to
     * sqlite3_reset(). The counter is incremented on the first sqlite3_step() call of each cycle.
     */
    RUN(6),

    /**
     * This is the number, corresponding to [FILTER_HIT], of times that the Bloom filter returned a
     * find, and thus the join step had to be processed as normal.
     */
    FILTER_MISS(7),

    /**
     * This is the number of times that a join step was bypassed because a Bloom filter returned
     * not-found.
     */
    FILTER_HIT(8),

    /**
     * This is the approximate number of bytes of heap memory used to store the prepared statement.
     * This value is not actually a counter, and so the resetFlg parameter to sqlite3_stmt_status()
     * is ignored when the opcode is SQLITE_STMTSTATUS_MEMUSED.
     */
    MEMUSED(99),
}