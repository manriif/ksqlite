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
@file:Suppress("ClassName", "SpellCheckingInspection", "RemoveExplicitTypeArguments")

package ksqlite.foreign.structs

import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsAny
import kotlin.js.JsBigInt
import kotlin.js.JsName
import kotlin.js.unsafeCast

/**
 * JS wrapper around `sqlite3_index_info` C-struct.
 */
public external interface sqlite3_index_info : StructType {

    @JsName($$"$nConstraint")
    public var nConstraint: Int

    @JsName($$"$aConstraint")
    public var aConstraint: WasmPointer

    @JsName($$"$nOrderBy")
    public var nOrderBy: Int

    @JsName($$"$aOrderBy")
    public var aOrderBy: WasmPointer

    @JsName($$"$aConstraintUsage")
    public var aConstraintUsage: WasmPointer

    @JsName($$"$idxNum")
    public var idxNum: Int

    @JsName($$"$idxStr")
    public var idxStr: WasmPointer

    @JsName($$"$needToFreeIdxStr")
    public var needToFreeIdxStr: Int

    @JsName($$"$orderByConsumed")
    public var orderByConsumed: Int

    @JsName($$"$estimatedCost")
    public var estimatedCost: Double

    @JsName($$"$estimatedRows")
    public var estimatedRows: JsBigInt

    @JsName($$"$idxFlags")
    public var idxFlags: Int

    @JsName($$"$colUsed")
    public var colUsed: JsBigInt

    public fun nthConstraint(
        n: Int,
        asPtr: Boolean
    ): JsAny

    public fun nthConstraintUsage(
        n: Int,
        asPtr: Boolean
    ): JsAny

    public fun nthOrderBy(
        n: Int,
        asPtr: Boolean
    ): JsAny
}

/**
 * JS wrapper around `sqlite3_index_info.sqlite3_index_constraint` C-struct.
 */
public external interface sqlite3_index_constraint : StructType {

    @JsName($$"$iColumn")
    public var iColumn: Int

    @JsName($$"$op")
    public var op: Int

    @JsName($$"$usable")
    public var usable: Int

    @JsName($$"$iTermOffset")
    public var iTermOffset: Int
}

/**
 * JS wrapper around `sqlite3_index_info.sqlite3_index_constraint_usage` C-struct.
 */
public external interface sqlite3_index_constraint_usage : StructType {

    @JsName($$"$argvIndex")
    public var argvIndex: Int

    @JsName($$"$omit")
    public var omit: Int
}

/**
 * JS wrapper around `sqlite3_index_info.sqlite3_index_orderby` C-struct.
 */
public external interface sqlite3_index_orderby : StructType {

    @JsName($$"$iColumn")
    public var iColumn: Int

    @JsName($$"$desc")
    public var desc: Int
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [sqlite3_index_constraint] at [index] of [sqlite3_index_info.aConstraint].
 */
public fun sqlite3_index_info.nthConstraint(index: Int): sqlite3_index_constraint =
    nthConstraint(index, false).unsafeCast<sqlite3_index_constraint>()

/**
 * Returns the [sqlite3_index_constraint_usage] at [index] of [sqlite3_index_info.aConstraintUsage].
 */
public fun sqlite3_index_info.nthConstraintUsage(index: Int): sqlite3_index_constraint_usage =
    nthConstraintUsage(index, false).unsafeCast<sqlite3_index_constraint_usage>()

/**
 * Returns the [sqlite3_index_orderby] at [index] of [sqlite3_index_info.aOrderBy].
 */
public fun sqlite3_index_info.nthOrderBy(index: Int): sqlite3_index_orderby =
    nthOrderBy(index, false).unsafeCast<sqlite3_index_orderby>()