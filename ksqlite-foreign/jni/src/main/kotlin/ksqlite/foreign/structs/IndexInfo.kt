@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.foreign.structs

import ksqlite.foreign.structLayout

/**
 * Wraps an instance of `sqlite3_index_info` at address [pointer] and supplies getters and setters
 * for reading and writing the struct.
 */
public class sqlite3_index_info(pointer: Long) : JniStruct(layout, StructType.IndexInfo, pointer) {

    public var nConstraint: Int
        get() = readInt(STRUCT_MEMBER_INDEX_NCONSTRAINT)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_NCONSTRAINT, value)

    public var aConstraint: Long
        get() = readLong(STRUCT_MEMBER_INDEX_ACONSTRAINT)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_ACONSTRAINT, value)

    public var nOrderBy: Int
        get() = readInt(STRUCT_MEMBER_INDEX_NORDERBY)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_NORDERBY, value)

    public var aOrderBy: Long
        get() = readLong(STRUCT_MEMBER_INDEX_AORDERBY)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_AORDERBY, value)

    public var aConstraintUsage: Long
        get() = readLong(STRUCT_MEMBER_INDEX_ACONSTRAINTUSAGE)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_ACONSTRAINTUSAGE, value)

    public var idxNum: Int
        get() = readInt(STRUCT_MEMBER_INDEX_IDXNUM)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_IDXNUM, value)

    public var idxStr: Long
        get() = readLong(STRUCT_MEMBER_INDEX_IDXSTR)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_IDXSTR, value)

    public var needToFreeIdxStr: Int
        get() = readInt(STRUCT_MEMBER_INDEX_NEEDTOFREEIDXSTR)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_NEEDTOFREEIDXSTR, value)

    public var orderByConsumed: Int
        get() = readInt(STRUCT_MEMBER_INDEX_ORDERBYCONSUMED)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_ORDERBYCONSUMED, value)

    public var estimatedCost: Double
        get() = readDouble(STRUCT_MEMBER_INDEX_ESTIMATEDCOST)
        set(value) = writeDouble(STRUCT_MEMBER_INDEX_ESTIMATEDCOST, value)

    public var estimatedRows: Long
        get() = readLong(STRUCT_MEMBER_INDEX_ESTIMATEDROWS)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_ESTIMATEDROWS, value)

    public var idxFlags: Int
        get() = readInt(STRUCT_MEMBER_INDEX_IDXFLAGS)
        set(value) = writeInt(STRUCT_MEMBER_INDEX_IDXFLAGS, value)

    public var colUsed: Long
        get() = readLong(STRUCT_MEMBER_INDEX_COLUSED)
        set(value) = writeLong(STRUCT_MEMBER_INDEX_COLUSED, value)

    /**
     * Returns the [sqlite3_index_constraint] at [index].
     */
    public fun constraint(index: Int): sqlite3_index_constraint = sqlite3_index_constraint(
        arrayItemAddress(aConstraint, index, sqlite3_index_constraint.layout)
    )

    /**
     * Returns the [sqlite3_index_constraint_usage] at [index].
     */
    public fun constraintUsage(index: Int): sqlite3_index_constraint_usage =
        sqlite3_index_constraint_usage(
            arrayItemAddress(aConstraint, index, sqlite3_index_constraint_usage.layout)
        )

    /**
     * Returns the [sqlite3_index_orderby] at [index].
     */
    public fun orderBy(index: Int): sqlite3_index_orderby =
        sqlite3_index_orderby(arrayItemAddress(aConstraint, index, sqlite3_index_orderby.layout))

    /**
     * Wraps an instance of `sqlite3_index_constraint` at address [pointer] and supplies getters and
     * setters for reading and writing the struct.
     */
    public class sqlite3_index_constraint(pointer: Long) :
        JniStruct(layout, StructType.IndexConstraint, pointer) {

        public var iColumn: Int
            get() = readInt(STRUCT_MEMBER_INDEX_ICOLUMN)
            set(value) = writeInt(STRUCT_MEMBER_INDEX_ICOLUMN, value)

        public var op: Byte
            get() = readByte(STRUCT_MEMBER_INDEX_OP)
            set(value) = writeByte(STRUCT_MEMBER_INDEX_OP, value)

        public var usable: Byte
            get() = readByte(STRUCT_MEMBER_INDEX_USABLE)
            set(value) = writeByte(STRUCT_MEMBER_INDEX_USABLE, value)

        public var iTermOffset: Int
            get() = readInt(STRUCT_MEMBER_INDEX_ITERMOFFSET)
            set(value) = writeInt(STRUCT_MEMBER_INDEX_ITERMOFFSET, value)

        public companion object Layout {

            internal val layout by lazy { structLayout(StructType.IndexConstraint) }

            public const val STRUCT_MEMBER_INDEX_ICOLUMN: Int = 0
            public const val STRUCT_MEMBER_INDEX_OP: Int = 1
            public const val STRUCT_MEMBER_INDEX_USABLE: Int = 2
            public const val STRUCT_MEMBER_INDEX_ITERMOFFSET: Int = 3
        }
    }

    /**
     * Wraps an instance of `sqlite3_index_constraint_usage` at address [pointer] and supplies
     * getters and setters for reading and writing the struct.
     */
    public class sqlite3_index_constraint_usage(pointer: Long) :
        JniStruct(layout, StructType.IndexConstraintUsage, pointer) {

        public var argvIndex: Int
            get() = readInt(STRUCT_MEMBER_INDEX_ARGVINDEX)
            set(value) = writeInt(STRUCT_MEMBER_INDEX_ARGVINDEX, value)

        public var omit: Byte
            get() = readByte(STRUCT_MEMBER_INDEX_OMIT)
            set(value) = writeByte(STRUCT_MEMBER_INDEX_OMIT, value)

        public companion object Layout {

            internal val layout by lazy { structLayout(StructType.IndexConstraintUsage) }

            public const val STRUCT_MEMBER_INDEX_ARGVINDEX: Int = 0
            public const val STRUCT_MEMBER_INDEX_OMIT: Int = 1
        }
    }

    /**
     * Wraps an instance of `sqlite3_index_orderby` at address [pointer] and supplies getters and 
     * setters for reading and writing the struct.
     */
    public class sqlite3_index_orderby(pointer: Long) :
        JniStruct(layout, StructType.IndexOrderby, pointer) {

        public var iColumn: Int
            get() = readInt(STRUCT_MEMBER_INDEX_ICOLUMN)
            set(value) = writeInt(STRUCT_MEMBER_INDEX_ICOLUMN, value)

        public var desc: Byte
            get() = readByte(STRUCT_MEMBER_INDEX_DESC)
            set(value) = writeByte(STRUCT_MEMBER_INDEX_DESC, value)

        public companion object Layout {

            internal val layout by lazy { structLayout(StructType.IndexOrderby) }

            public const val STRUCT_MEMBER_INDEX_ICOLUMN: Int = 0
            public const val STRUCT_MEMBER_INDEX_DESC: Int = 1
        }
    }

    public companion object Layout {

        internal val layout by lazy { structLayout(StructType.IndexInfo) }

        public const val STRUCT_MEMBER_INDEX_NCONSTRAINT: Int = 0
        public const val STRUCT_MEMBER_INDEX_ACONSTRAINT: Int = 1
        public const val STRUCT_MEMBER_INDEX_NORDERBY: Int = 2
        public const val STRUCT_MEMBER_INDEX_AORDERBY: Int = 3
        public const val STRUCT_MEMBER_INDEX_ACONSTRAINTUSAGE: Int = 4
        public const val STRUCT_MEMBER_INDEX_IDXNUM: Int = 5
        public const val STRUCT_MEMBER_INDEX_IDXSTR: Int = 6
        public const val STRUCT_MEMBER_INDEX_NEEDTOFREEIDXSTR: Int = 7
        public const val STRUCT_MEMBER_INDEX_ORDERBYCONSUMED: Int = 8
        public const val STRUCT_MEMBER_INDEX_ESTIMATEDCOST: Int = 9
        public const val STRUCT_MEMBER_INDEX_ESTIMATEDROWS: Int = 10
        public const val STRUCT_MEMBER_INDEX_IDXFLAGS: Int = 11
        public const val STRUCT_MEMBER_INDEX_COLUSED: Int = 12
    }
}