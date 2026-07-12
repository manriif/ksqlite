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
package ksqlite.kapi.database

/**
 * Result of [DatabaseConnection.tableColumnMetadata].
 * Member fields are all already resolved.
 */
public interface TableColumnMetadata {

    /**
     * Column data type.
     */
    public val dataType: String

    /**
     * Name of the default collation sequence.
     */
    public val collationSequence: String

    /**
     * Whether the column has NOT NULL constraint.
     */
    public val isNullable: Boolean

    /**
     * Whether the column is part of the PRIMARY KEY.
     */
    public val isPrimaryKey: Boolean

    /**
     * Whether the column is AUTOINCREMENT.
     */
    public val isAutoIncrement: Boolean
}