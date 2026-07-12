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
@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.memory.Struct
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_mprintf
import ksqlite.types.internal.convertVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteIndexInfo
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteVtabScanFlag

public actual class sqlite3_index_info
internal constructor(override val pointer: CPointer<s3_index_info>) :
    Struct(pointer),
    SqliteIndexInfo {

    private inline val info: s3_index_info
        get() = pointer.pointed

    public actual override val colUsed: ULong
        get() = info.colUsed

    public actual override val nConstraint: Int
        get() = info.nConstraint

    public actual override val nOrderBy: Int
        get() = info.nOrderBy

    public actual override fun getConstraintColumn(index: Int): Int =
        info.aConstraint!![index].iColumn

    public actual override fun getConstraintOp(index: Int): SqliteVtabConstraintOperatorCode =
        convertVtabConstraintOperatorCode(info.aConstraint!![index].op.toInt())

    public actual override fun getConstraintUsable(index: Int): Int =
        info.aConstraint!![index].usable.toInt()

    public actual override fun getOrderByColumn(index: Int): Int =
        info.aOrderBy!![index].iColumn

    public actual override fun getOrderByDesc(index: Int): Int =
        info.aOrderBy!![index].desc.toInt()

    public actual override var idxNum: Int
        get() = info.idxNum
        set(value) {
            info.idxNum = value
        }

    public actual override var idxStr: String?
        get() = info.idxStr?.toKStringFromUtf8()
        set(value) {
            sqlite3_free(info.idxStr)
            info.idxStr = value?.let { sqlite3_mprintf(it) }
        }

    public actual override var needToFreeIdxStr: Int
        get() = info.needToFreeIdxStr
        set(value) {
            info.needToFreeIdxStr = value
        }

    public actual override var orderByConsumed: Int
        get() = info.orderByConsumed
        set(value) {
            info.orderByConsumed = value
        }

    public actual override var estimatedCost: Double
        get() = info.estimatedCost
        set(value) {
            info.estimatedCost = value
        }

    public actual override var estimatedRows: Long
        get() = info.estimatedRows
        set(value) {
            info.estimatedRows = value
        }

    public actual override var idxFlags: SqliteVtabScanFlag
        get() = SqliteVtabScanFlag.from(info.idxFlags)
        set(value) {
            info.idxFlags = value.value
        }

    public actual override fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int) {
        info.aConstraintUsage!![index].argvIndex = argvIndex
    }

    public actual override fun setConstraintUsageOmit(index: Int, omit: Int) {
        info.aConstraintUsage!![index].omit = omit.convert()
    }
}