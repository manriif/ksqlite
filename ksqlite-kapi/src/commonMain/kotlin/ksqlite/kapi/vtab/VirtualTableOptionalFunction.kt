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
 * Optional functions of a [VirtualTable].
 */
public enum class VirtualTableOptionalFunction {
    /**
     * Represents [VirtualTable.update].
     */
    Update,

    /**
     * Represents [VirtualTable.findFunction].
     */
    FindFunction,

    /**
     * Represents [VirtualTable.begin].
     */
    Begin,

    /**
     * Represents [VirtualTable.sync].
     */
    Sync,

    /**
     * Represents [VirtualTable.commit].
     */
    Commit,

    /**
     * Represents [VirtualTable.rollback].
     */
    Rollback,

    /**
     * Represents [VirtualTable.rename].
     */
    Rename,

    /**
     * Represents [VirtualTable.savepoint].
     */
    Savepoint,

    /**
     * Represents [VirtualTable.release].
     */
    Release,

    /**
     * Represents [VirtualTable.rollbackTo].
     */
    RollbackTo,

    /**
     * Represents [VirtualTable.integrity].
     */
    Integrity
}