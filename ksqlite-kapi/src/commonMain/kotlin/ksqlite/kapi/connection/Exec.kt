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
package ksqlite.kapi.connection

/**
 * Callback to use with [DatabaseConnection.execute].
 */
public fun interface Exec {

    /**
     * Called once per result row. [columnCount] is the number of columns in the row,
     * [columnValues] and [columnNames] hold the value and name of each column and must not be
     * accessed past index [columnCount] even if they contain more elements. Returns `true` to
     * abort the execution, `false` to let it continue.
     */
    public fun apply(
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Boolean
}