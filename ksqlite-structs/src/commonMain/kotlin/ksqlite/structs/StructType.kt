package ksqlite.structs

import ksqlite.structs.sqlite3_index_info.sqlite3_index_constraint
import ksqlite.structs.sqlite3_index_info.sqlite3_index_constraint_usage
import ksqlite.structs.sqlite3_index_info.sqlite3_index_orderby

/**
 * Structs types as recognized in the Ksqlite C side.
 */
public sealed class StructType<Type, Member>(public val value: Int)
        where Type : StructType<Type, Member>, Member : StructMember<Type> {

    public data object Sqlite3IndexInfo :
        StructType<Sqlite3IndexInfo, sqlite3_index_info.Member>(0)

    public data object Sqlite3IndexConstraint :
        StructType<Sqlite3IndexConstraint, sqlite3_index_constraint.Member>(1)

    public data object Sqlite3IndexConstraintUsage :
        StructType<Sqlite3IndexConstraintUsage, sqlite3_index_constraint_usage.Member>(2)

    public data object Sqlite3IndexOrderby :
        StructType<Sqlite3IndexOrderby, sqlite3_index_orderby.Member>(3)

    public data object Sqlite3Module :
        StructType<Sqlite3Module, sqlite3_module.Member>(4)

    public data object Sqlite3Vtab :
        StructType<Sqlite3Vtab, sqlite3_vtab.Member>(5)

    public data object Sqlite3VtabCursor :
        StructType<Sqlite3VtabCursor, sqlite3_vtab_cursor.Member>(6)

    public data object Sqlite3File :
        StructType<Sqlite3File, sqlite3_file.Member>(7)

    public data object Sqlite3IoMethods :
        StructType<Sqlite3IoMethods, sqlite3_io_methods.Member>(8)

    public data object Sqlite3Vfs :
        StructType<Sqlite3Vfs, sqlite3_vfs.Member>(9)

    public data object KsqliteCipherDescriptor :
        StructType<KsqliteCipherDescriptor, ksqlite_cipher_descriptor.Member>(10)

    public data object KsqliteCipherParams :
        StructType<KsqliteCipherParams, ksqlite_cipher_params.Member>(11)
}

/**
 * [StructType] with erased parameters.
 */
public typealias RawStructType = StructType<*, *>

///////////////////////////////////////////////////////////////////////////
// Layout
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [StructLayout] for this [StructType].
 */
internal val RawStructType.layout: StructLayout
    get() = structLayoutProvider.provide(this)

/**
 * Returns the whole struct size in bytes.
 */
public val RawStructType.structSize: Int
    get() = layout.structSize

/**
 * Returns the offset of [member].
 */
public fun <Member : StructMember<*>> StructType<*, Member>.offsetOf(member: Member): Int =
    layout.memberOffset(member.ordinal)

/**
 * Returns the size of [member].
 */
public fun <Member : StructMember<*>> StructType<*, Member>.sizeOf(member: Member): Int =
    layout.memberSize(member.ordinal)