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

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_preupdate_blobwrite
import ksqlite.capi.sqlite3_preupdate_count
import ksqlite.capi.sqlite3_preupdate_depth
import ksqlite.capi.sqlite3_preupdate_new
import ksqlite.capi.sqlite3_preupdate_old
import ksqlite.capi.sqlite3_value
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.value.ProtectedValue
import ksqlite.kapi.value.toProtectedValue

internal class PreupdateHookScopeImpl(private val db: sqlite3) :
    PreupdateHookScope,
    UnsafeCloseableScope() {

    override val count: Int
        get() = notClosed { sqlite3_preupdate_count(db) }

    override val depth: Int
        get() = notClosed { sqlite3_preupdate_depth(db) }

    override val blobColumnIndex: Int
        get() = notClosed { sqlite3_preupdate_blobwrite(db) }

    override fun oldValue(index: Int): ProtectedValue = notClosed {
        usingParam(sqlite3_value.OutputParam()) { outValue ->
            sqliteResultCheck(sqlite3_preupdate_old(db, index, outValue))
        }.toProtectedValue(this)
    }

    override fun newValue(index: Int): ProtectedValue = notClosed {
        usingParam(sqlite3_value.OutputParam()) { outValue ->
            sqliteResultCheck(sqlite3_preupdate_new(db, index, outValue))
        }.toProtectedValue(this)
    }
}