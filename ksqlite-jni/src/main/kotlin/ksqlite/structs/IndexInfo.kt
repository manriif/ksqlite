@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.structs

import ksqlite.structLayout

/**
 * Wraps an instance of `sqlite3_index_info` at address [pointer] and supplies getters and setters
 * for reading and writing the struct.
 */
public class sqlite3_index_info(pointer: Long) : JniStruct(pointer, layout) {

    public var nConstraint: Int
        get() = readInt(LAYOUT_INDEX_NCONSTRAINT)
        set(value) = writeInt(LAYOUT_INDEX_NCONSTRAINT, value)

    public var aConstraint: Long
        get() = readLong(LAYOUT_INDEX_ACONSTRAINT)
        set(value) = writeLong(LAYOUT_INDEX_ACONSTRAINT, value)

    public var nOrderBy: Int
        get() = readInt(LAYOUT_INDEX_NORDERBY)
        set(value) = writeInt(LAYOUT_INDEX_NORDERBY, value)

    public var aOrderBy: Long
        get() = readLong(LAYOUT_INDEX_AORDERBY)
        set(value) = writeLong(LAYOUT_INDEX_AORDERBY, value)

    public var aConstraintUsage: Long
        get() = readLong(LAYOUT_INDEX_ACONSTRAINTUSAGE)
        set(value) = writeLong(LAYOUT_INDEX_ACONSTRAINTUSAGE, value)

    public var idxNum: Int
        get() = readInt(LAYOUT_INDEX_IDXNUM)
        set(value) = writeInt(LAYOUT_INDEX_IDXNUM, value)

    public var idxStr: Long
        get() = readLong(LAYOUT_INDEX_IDXSTR)
        set(value) = writeLong(LAYOUT_INDEX_IDXSTR, value)

    public var needToFreeIdxStr: Int
        get() = readInt(LAYOUT_INDEX_NEEDTOFREEIDXSTR)
        set(value) = writeInt(LAYOUT_INDEX_NEEDTOFREEIDXSTR, value)

    public var orderByConsumed: Int
        get() = readInt(LAYOUT_INDEX_ORDERBYCONSUMED)
        set(value) = writeInt(LAYOUT_INDEX_ORDERBYCONSUMED, value)

    public var estimatedCost: Double
        get() = readDouble(LAYOUT_INDEX_ESTIMATEDCOST)
        set(value) = writeDouble(LAYOUT_INDEX_ESTIMATEDCOST, value)

    public var estimatedRows: Long
        get() = readLong(LAYOUT_INDEX_ESTIMATEDROWS)
        set(value) = writeLong(LAYOUT_INDEX_ESTIMATEDROWS, value)

    public var idxFlags: Int
        get() = readInt(LAYOUT_INDEX_IDXFLAGS)
        set(value) = writeInt(LAYOUT_INDEX_IDXFLAGS, value)

    public var colUsed: Long
        get() = readLong(LAYOUT_INDEX_COLUSED)
        set(value) = writeLong(LAYOUT_INDEX_COLUSED, value)

    /**
     * Wraps an instance of `sqlite3_index_constraint` at address [pointer] and supplies getters and
     * setters for reading and writing the struct.
     */
    public class sqlite3_index_constraint(pointer: Long) : JniStruct(pointer, layout) {

        public var iColumn: Int
            get() = readInt(LAYOUT_INDEX_ICOLUMN)
            set(value) = writeInt(LAYOUT_INDEX_ICOLUMN, value)

        public var op: Byte
            get() = readByte(LAYOUT_INDEX_OP)
            set(value) = writeByte(LAYOUT_INDEX_OP, value)

        public var usable: Byte
            get() = readByte(LAYOUT_INDEX_USABLE)
            set(value) = writeByte(LAYOUT_INDEX_USABLE, value)

        public var iTermOffset: Int
            get() = readInt(LAYOUT_INDEX_ITERMOFFSET)
            set(value) = writeInt(LAYOUT_INDEX_ITERMOFFSET, value)

        private companion object Layout {

            val layout by lazy { structLayout(StructType.IndexConstraint) }

            const val LAYOUT_INDEX_ICOLUMN = 0
            const val LAYOUT_INDEX_OP = 1
            const val LAYOUT_INDEX_USABLE = 2
            const val LAYOUT_INDEX_ITERMOFFSET = 3
        }
    }

    /**
     * Wraps an instance of `sqlite3_index_constraint_usage` at address [pointer] and supplies
     * getters and setters for reading and writing the struct.
     */
    public class sqlite3_index_constraint_usage(pointer: Long) : JniStruct(pointer, layout) {

        public var argvIndex: Int
            get() = readInt(LAYOUT_INDEX_ARGVINDEX)
            set(value) = writeInt(LAYOUT_INDEX_ARGVINDEX, value)

        public var omit: Byte
            get() = readByte(LAYOUT_INDEX_OMIT)
            set(value) = writeByte(LAYOUT_INDEX_OMIT, value)

        private companion object Layout {

            val layout by lazy { structLayout(StructType.IndexConstraintUsage) }

            const val LAYOUT_INDEX_ARGVINDEX = 0
            const val LAYOUT_INDEX_OMIT = 1
        }
    }

    /**
     * Wraps an instance of `sqlite3_index_orderby` at address [pointer] and supplies getters and 
     * setters for reading and writing the struct.
     */
    public class sqlite3_index_orderby(pointer: Long) : JniStruct(pointer, layout) {

        public var iColumn: Int
            get() = readInt(LAYOUT_INDEX_ICOLUMN)
            set(value) = writeInt(LAYOUT_INDEX_ICOLUMN, value)

        public var desc: Byte
            get() = readByte(LAYOUT_INDEX_DESC)
            set(value) = writeByte(LAYOUT_INDEX_DESC, value)

        private companion object Layout {

            val layout by lazy { structLayout(StructType.IndexOrderby) }

            const val LAYOUT_INDEX_ICOLUMN = 0
            const val LAYOUT_INDEX_DESC = 1
        }
    }

    private companion object Layout {

        val layout by lazy { structLayout(StructType.IndexInfo) }

        const val LAYOUT_INDEX_NCONSTRAINT = 0
        const val LAYOUT_INDEX_ACONSTRAINT = 1
        const val LAYOUT_INDEX_NORDERBY = 2
        const val LAYOUT_INDEX_AORDERBY = 3
        const val LAYOUT_INDEX_ACONSTRAINTUSAGE = 4
        const val LAYOUT_INDEX_IDXNUM = 5
        const val LAYOUT_INDEX_IDXSTR = 6
        const val LAYOUT_INDEX_NEEDTOFREEIDXSTR = 7
        const val LAYOUT_INDEX_ORDERBYCONSUMED = 8
        const val LAYOUT_INDEX_ESTIMATEDCOST = 9
        const val LAYOUT_INDEX_ESTIMATEDROWS = 10
        const val LAYOUT_INDEX_IDXFLAGS = 11
        const val LAYOUT_INDEX_COLUSED = 12
    }
}