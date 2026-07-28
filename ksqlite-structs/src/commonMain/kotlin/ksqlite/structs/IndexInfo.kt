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

package ksqlite.structs

/**
 * Allocates or reinterprets a `sqlite3_index_info`.
 */
public abstract class sqlite3_index_info<Pointer : Any>(
    adapter: Adapter<Pointer>,
    pointer: Pointer?
) : Struct<StructType.Sqlite3IndexInfo, sqlite3_index_info.Member, Pointer>(
    type = Sqlite3IndexInfo,
    adapter = adapter,
    pointer = pointer
) {

    public var nConstraint: Int
        get() = readInt(NCONSTRAINT)
        set(value) = writeInt(NCONSTRAINT, value)

    public var aConstraint: Pointer
        get() = readPointer(ACONSTRAINT)
        set(value) = writePointer(ACONSTRAINT, value)

    public var nOrderBy: Int
        get() = readInt(NORDERBY)
        set(value) = writeInt(NORDERBY, value)

    public var aOrderBy: Pointer
        get() = readPointer(AORDERBY)
        set(value) = writePointer(AORDERBY, value)

    public var aConstraintUsage: Pointer
        get() = readPointer(ACONSTRAINTUSAGE)
        set(value) = writePointer(ACONSTRAINTUSAGE, value)

    public var idxNum: Int
        get() = readInt(IDXNUM)
        set(value) = writeInt(IDXNUM, value)

    public var idxStr: Pointer
        get() = readPointer(IDXSTR)
        set(value) = writePointer(IDXSTR, value)

    public var needToFreeIdxStr: Int
        get() = readInt(NEEDTOFREEIDXSTR)
        set(value) = writeInt(NEEDTOFREEIDXSTR, value)

    public var orderByConsumed: Int
        get() = readInt(ORDERBYCONSUMED)
        set(value) = writeInt(ORDERBYCONSUMED, value)

    public var estimatedCost: Double
        get() = readDouble(ESTIMATEDCOST)
        set(value) = writeDouble(ESTIMATEDCOST, value)

    public var estimatedRows: Long
        get() = readLong(ESTIMATEDROWS)
        set(value) = writeLong(ESTIMATEDROWS, value)

    public var idxFlags: Int
        get() = readInt(IDXFLAGS)
        set(value) = writeInt(IDXFLAGS, value)

    public var colUsed: ULong
        get() = readULong(COLUSED)
        set(value) = writeULong(COLUSED, value)

    /**
     * Returns the [sqlite3_index_constraint] at [index].
     */
    public fun constraint(index: Int): sqlite3_index_constraint<Pointer> = arrayItem(
        baseAddress = aConstraint,
        itemIndex = index,
        itemType = Sqlite3IndexConstraint,
        factory = ::sqlite3_index_constraint
    )

    /**
     * Returns the [sqlite3_index_constraint_usage] at [index].
     */
    public fun constraintUsage(index: Int): sqlite3_index_constraint_usage<Pointer> = arrayItem(
        baseAddress = aConstraintUsage,
        itemIndex = index,
        itemType = Sqlite3IndexConstraintUsage,
        factory = ::sqlite3_index_constraint_usage
    )

    /**
     * Returns the [sqlite3_index_orderby] at [index].
     */
    public fun orderBy(index: Int): sqlite3_index_orderby<Pointer> = arrayItem(
        baseAddress = aOrderBy,
        itemIndex = index,
        itemType = Sqlite3IndexOrderby,
        factory = ::sqlite3_index_orderby
    )

    /**
     * Wraps an instance of `sqlite3_index_constraint`.
     */
    public class sqlite3_index_constraint<Pointer : Any> internal constructor(
        adapter: Adapter<Pointer>,
        pointer: Pointer
    ) : Struct<StructType.Sqlite3IndexConstraint, sqlite3_index_constraint.Member, Pointer>(
        type = Sqlite3IndexConstraint,
        adapter = adapter,
        pointer = pointer
    ) {

        public var iColumn: Int
            get() = readInt(ICOLUMN)
            set(value) = writeInt(ICOLUMN, value)

        public var op: UByte
            get() = readUByte(OP)
            set(value) = writeUByte(OP, value)

        public var usable: UByte
            get() = readUByte(USABLE)
            set(value) = writeUByte(USABLE, value)

        public var iTermOffset: Int
            get() = readInt(ITERMOFFSET)
            set(value) = writeInt(ITERMOFFSET, value)

        /**
         * Members of the `sqlite3_index_constraint` struct.
         */
        public enum class Member : StructMember<StructType.Sqlite3IndexConstraint> {
            ICOLUMN,
            OP,
            USABLE,
            ITERMOFFSET,
        }
    }

    /**
     * Wraps an instance of `sqlite3_index_constraint_usage`.
     */
    public class sqlite3_index_constraint_usage<Pointer : Any> internal constructor(
        adapter: Adapter<Pointer>,
        pointer: Pointer
    ) : Struct<StructType.Sqlite3IndexConstraintUsage, sqlite3_index_constraint_usage.Member, Pointer>(
        type = Sqlite3IndexConstraintUsage,
        adapter = adapter,
        pointer = pointer
    ) {

        public var argvIndex: Int
            get() = readInt(ARGVINDEX)
            set(value) = writeInt(ARGVINDEX, value)

        public var omit: UByte
            get() = readUByte(OMIT)
            set(value) = writeUByte(OMIT, value)

        /**
         * Members of the `sqlite3_index_constraint_usage` struct.
         */
        public enum class Member : StructMember<StructType.Sqlite3IndexConstraintUsage> {
            ARGVINDEX,
            OMIT,
        }
    }

    /**
     * Wraps an instance of `sqlite3_index_orderby`.
     */
    public class sqlite3_index_orderby<Pointer : Any> internal constructor(
        adapter: Adapter<Pointer>,
        pointer: Pointer
    ) : Struct<StructType.Sqlite3IndexOrderby, sqlite3_index_orderby.Member, Pointer>(
        type = Sqlite3IndexOrderby,
        adapter = adapter,
        pointer = pointer
    ) {

        public var iColumn: Int
            get() = readInt(ICOLUMN)
            set(value) = writeInt(ICOLUMN, value)

        public var desc: Byte
            get() = readByte(DESC)
            set(value) = writeByte(DESC, value)

        /**
         * Members of the `sqlite3_index_orderby` struct.
         */
        public enum class Member : StructMember<StructType.Sqlite3IndexOrderby> {
            ICOLUMN,
            DESC,
        }
    }

    /**
     * Members of the `sqlite3_index_info` struct.
     */
    public enum class Member : StructMember<StructType.Sqlite3IndexInfo> {
        NCONSTRAINT,
        ACONSTRAINT,
        NORDERBY,
        AORDERBY,
        ACONSTRAINTUSAGE,
        IDXNUM,
        IDXSTR,
        NEEDTOFREEIDXSTR,
        ORDERBYCONSUMED,
        ESTIMATEDCOST,
        ESTIMATEDROWS,
        IDXFLAGS,
        COLUSED,
    }
}