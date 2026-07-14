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
package ksqlite.capi.vfs.callbacks

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.SqliteResultCode

/**
 * Represents the [sqlite3_vfs.xDelete] callback.
 */
public fun interface SqliteVfsDeleteCallback {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/vfs.html).
     */
    public fun apply(
        vfs: sqlite3_vfs,
        name: String,
        syncDir: Int,
    ): SqliteResultCode
}