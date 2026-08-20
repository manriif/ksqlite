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
package ksqlite.kapi.vtab

import co.touchlab.stately.concurrency.Lock
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.connection.DatabaseConnection

/**
 * [Virtual Table Module](https://sqlite.org/vtab.html#implementation).
 *
 * This interface is not directly implementable, one of [Regular], [Eponymous] or [EponymousOnly]
 * must be implemented depending on the virtual table kind.
 *
 * Note that a [VirtualTableModule] cannot be a singleton nor a reusable instance — a new instance
 * must always be passed to [DatabaseConnection.createModule].
 */
public sealed class VirtualTableModule : AutoCloseable {

    internal var module: sqlite3_module<*>? = null
    internal val moduleLock = Lock()

    ///////////////////////////////////////////////////////////////////////////
    // Kinds
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Regular Virtual Table.
     * Distinct callbacks are passed to SQLite for [create] and [connect].
     */
    public abstract class Regular : VirtualTableModule() {

        /**
         * Creates and connects to a virtual table and returns the [VirtualTable] instance.
         * If an error is detected, an [SQLiteException] is thrown and is returned to SQLite.
         */
        public abstract fun VirtualTableCreateOrConnectScope.create(
            connection: DatabaseConnection,
            arguments: Array<String>
        ): VirtualTable
    }

    /**
     * [Eponymous Virtual Table](https://sqlite.org/vtab.html#eponymous_virtual_tables).
     * The same callback reference is passed to SQLite for both create and [connect].
     */
    public abstract class Eponymous : VirtualTableModule()

    /**
     * [Eponymous Only Virtual Table](https://sqlite.org/vtab.html#eponymous_only_virtual_tables).
     * A callback is passed to SQLite for [connect] but not for create.
     */
    public abstract class EponymousOnly : VirtualTableModule()

    ///////////////////////////////////////////////////////////////////////////
    // Common
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the optional virtual table functions supported by the module.
     * Only the functions that are returned are invoked by SQLite.
     */
    public open fun optionalFunctions(): Set<VirtualTableOptionalFunction> = emptySet()

    /**
     * Connects to an already created virtual table and returns the [VirtualTable] instance.
     * If an error is detected, an [SQLiteException] is thrown and is returned to SQLite.
     */
    public abstract fun VirtualTableCreateOrConnectScope.connect(
        connection: DatabaseConnection,
        arguments: Array<String>
    ): VirtualTable

    /**
     * Called when the module is finalized by SQLite. Finalization can also happen when the
     * module registration fails.
     */
    override fun close(): Unit = Unit
}