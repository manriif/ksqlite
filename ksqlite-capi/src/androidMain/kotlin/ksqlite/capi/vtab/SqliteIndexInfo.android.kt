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

import ksqlite.capi.memory.JniPointer
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.types.internal.convertVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteIndexInfo
import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode
import ksqlite.types.vtab.SqliteVtabScanFlag

public actual class sqlite3_index_info private constructor(private val info: s3_index_info) :
    Struct(info.pointer),
    SqliteIndexInfo {

    internal constructor(pointer: JniPointer) : this(s3_index_info(pointer))

    private val constraints by lazy { Array(nConstraint, info::constraint) }
    private val constraintUsages by lazy { Array(nConstraint, info::constraintUsage) }
    private val orderBys by lazy { Array(nConstraint, info::orderBy) }

    public actual override val colUsed: ULong
        get() = info.colUsed.toULong()

    public actual override val nConstraint: Int
        get() = info.nConstraint

    public actual override val nOrderBy: Int
        get() = info.nOrderBy

    public actual override fun getConstraintColumn(index: Int): Int =
        constraints[index].iColumn

    public actual override fun getConstraintOp(index: Int): SqliteVtabConstraintOperatorCode =
        convertVtabConstraintOperatorCode(constraints[index].op.toInt())

    public actual override fun getConstraintUsable(index: Int): Int =
        constraints[index].usable.toInt()

    public actual override fun getOrderByColumn(index: Int): Int =
        orderBys[index].iColumn

    public actual override fun getOrderByDesc(index: Int): Int =
        orderBys[index].desc.toInt()

    public actual override var idxNum: Int
        get() = info.idxNum
        set(value) {
            info.idxNum = value
        }

    public actual override var idxStr: String?
        get() = info.idxStr.toKStringFromUtf8OrNull()
        set(value) = sqlite3_mprintf(info::idxStr, value)

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
        constraintUsages[index].argvIndex = argvIndex
    }

    public actual override fun setConstraintUsageOmit(index: Int, omit: Int) {
        constraintUsages[index].omit = omit.toByte()
    }
}