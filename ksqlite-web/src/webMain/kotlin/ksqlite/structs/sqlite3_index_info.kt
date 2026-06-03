@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.wasm.WasmPointer
import kotlin.js.JsBigInt

/**
 * JS wrapper around `sqlite3_index_info` C-struct.
 */
public external class sqlite3_index_info : JaccwabytStruct {

    /**
     * Allocates an instance wrapping the existing instance pointed by [pointer].
     */
    public constructor(pointer: WasmPointer)

    public var nConstraint: Int
    public var aConstraint: WasmPointer
    public var nOrderBy: Int
    public var aOrderBy: WasmPointer
    public var aConstraintUsage: WasmPointer
    public var idxNum: Int
    public var idxStr: WasmPointer
    public var needToFreeIdxStr: Int
    public var orderByConsumed: Int
    public var estimatedCost: Double
    public var estimatedRows: JsBigInt
    public var idxFlags: Int
    public var colUsed: JsBigInt
}

public external class sqlite3_index_constraint {

    public var iColumn: Int
    public var op: UByte
    public var usable: UByte
    public var iTermOffset: Int
}

public external class sqlite3_index_orderby {

    public var iColumn: Int
    public var desc: UByte
}

public external class sqlite3_index_constraint_usage {

    public var argvIndex: Int
    public var omit: Int
}