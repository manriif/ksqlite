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

import ksqlite.kapi.SQLite
import ksqlite.kapi.SQLiteException

/**
 * Callback to use with [SQLite.addAutoExtension] and [SQLite.removeAutoExtension].
 */
public fun interface AutoExtension {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/auto_extension.html).
     *
     * If an error is detected, then an [SQLiteException] should be thrown. Other exception types
     * are not caught.
     */
    public fun apply(connection: DatabaseConnection)
}