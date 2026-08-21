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

package ksqlite.kapi.vtab

/**
 * Exposes Virtual Table configuration API.
 *
 * [Virtual Table Configuration Options](https://sqlite.org/c3ref/c_vtab_constraint_support.html)
 */
public interface VirtualTableConfiguration {

    /**
     * Sets whether the virtual table implementation guarantees that when an update fails due to
     * a constraint violation, it does so before making any change to internal or persistent
     * data structures.
     */
    public fun setConstraintSupportEnabled(enabled: Boolean)

    /**
     * Marks the virtual table as being safe to use from within triggers and views.
     */
    public fun setInnocuous()

    /**
     * Prohibits the use of the virtual table from within triggers and views.
     */
    public fun setDirectOnly()

    /**
     * Instructs the query planner to begin at least a read transaction on all schemas ("main",
     * "temp", and any ATTACH-ed databases) whenever the virtual table is used.
     */
    public fun setUsesAllSchemas()
}