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
@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.vtab

import ksqlite.capi.memory.CloseableStruct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.foreign.sqlite3
import ksqlite.types.internal.convertVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteIndexInfo
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteVtabScanFlag
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import ksqlite.foreign.sqlite3_index_info.sqlite3_index_constraint as s3_index_constraint
import ksqlite.foreign.sqlite3_index_info.sqlite3_index_constraint_usage as s3_index_constraint_usage
import ksqlite.foreign.sqlite3_index_info.sqlite3_index_orderby as s3_index_orderby

public actual class sqlite3_index_info internal constructor(pointer: MemorySegment) :
    CloseableStruct(s3_index_info.layout(), pointer, External, Arena.ofConfined()),
    SqliteIndexInfo {

    private val constraints by lazy {
        val aConstraint = s3_index_info.aConstraint(this.pointer)
        Array(nConstraint) { s3_index_constraint.asSlice(aConstraint, it.toLong()) }
    }

    private val constraintUsages by lazy {
        val aConstraintUsage = s3_index_info.aConstraintUsage(this.pointer)
        Array(nConstraint) { s3_index_constraint_usage.asSlice(aConstraintUsage, it.toLong()) }
    }

    private val orderBys by lazy {
        val aOrderBy = s3_index_info.aOrderBy(this.pointer)
        Array(nConstraint) { s3_index_orderby.asSlice(aOrderBy, it.toLong()) }
    }

    public actual override val colUsed: ULong
        get() = s3_index_info.colUsed(pointer).toULong()

    public actual override val nConstraint: Int
        get() = s3_index_info.nConstraint(pointer)

    public actual override val nOrderBy: Int
        get() = s3_index_info.nOrderBy(pointer)

    public actual override fun getConstraintColumn(index: Int): Int =
        s3_index_constraint.iColumn(constraints[index])

    public actual override fun getConstraintOp(index: Int): SqliteVtabConstraintOperatorCode =
        convertVtabConstraintOperatorCode(s3_index_constraint.op(constraints[index]).toInt())

    public actual override fun getConstraintUsable(index: Int): Int =
        s3_index_constraint.usable(constraints[index]).toInt()

    public actual override fun getOrderByColumn(index: Int): Int =
        s3_index_orderby.iColumn(orderBys[index])

    public actual override fun getOrderByDesc(index: Int): Int =
        s3_index_orderby.desc(orderBys[index]).toInt()

    public actual override var idxNum: Int
        get() = s3_index_info.idxNum(pointer)
        set(value) = s3_index_info.idxNum(pointer, value)

    public actual override var idxStr: String?
        get() = s3_index_info.idxStr(pointer).toKStringFromUtf8OrNull()
        set(value) {
            sqlite3.sqlite3_free(s3_index_info.idxStr(pointer))
            s3_index_info.idxStr(pointer, sqlite3_mprintf(value))
        }

    public actual override var needToFreeIdxStr: Int
        get() = s3_index_info.needToFreeIdxStr(pointer)
        set(value) = s3_index_info.needToFreeIdxStr(pointer, value)

    public actual override var orderByConsumed: Int
        get() = s3_index_info.orderByConsumed(pointer)
        set(value) = s3_index_info.orderByConsumed(pointer, value)

    public actual override var estimatedCost: Double
        get() = s3_index_info.estimatedCost(pointer)
        set(value) = s3_index_info.estimatedCost(pointer, value)

    public actual override var estimatedRows: Long
        get() = s3_index_info.estimatedRows(pointer)
        set(value) = s3_index_info.estimatedRows(pointer, value)

    public actual override var idxFlags: SqliteVtabScanFlag
        get() = SqliteVtabScanFlag.from(s3_index_info.idxFlags(pointer))
        set(value) = s3_index_info.idxFlags(pointer, value.value)

    public actual override fun setConstraintUsageArgvIndex(index: Int, argvIndex: Int) {
        s3_index_constraint_usage.argvIndex(constraintUsages[index], argvIndex)
    }

    public actual override fun setConstraintUsageOmit(index: Int, omit: Int) {
        s3_index_constraint_usage.omit(constraintUsages[index], omit.toByte())
    }
}