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

/**
 * Scope to use with [VirtualTableModule.connect] and [VirtualTableModule.Regular.create].
 */
public interface VirtualTableCreateOrConnectScope {

    /**
     * Configuration of the virtual table.
     */
    public val config: VirtualTableConfiguration

    /**
     * Declares the schema of the virtual table.
     */
    public fun declare(sql: String)

    /**
     * Declares that the virtual table overloads the function identified by [name] and
     * [argumentCount].
     *
     * The virtual table must implement [VirtualTable.findFunction].
     */
    public fun overloadFunction(
        name: String,
        argumentCount: Int
    )
}