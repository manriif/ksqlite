package ksqlite.capi.memory

import kotlin.jvm.JvmInline

/**
 * Value of a variadic function call.
 */
internal sealed interface VariadicValue<out Pointer : Any> {

    val value: Any

    @JvmInline
    value class OfPointer<P : Any>(override val value: P) : VariadicValue<P>

    @JvmInline
    value class OfInt(override val value: Int) : VariadicValue<Nothing>

    @JvmInline
    value class OfUInt(override val value: UInt) : VariadicValue<Nothing>

    @JvmInline
    value class OfLong(override val value: Long) : VariadicValue<Nothing>

    /**
     * Strings are given a key because it is possible that the application must manage it as SQLite
     * won't make a copy of it.
     *
     * For now, [ksqlite.capi.types.SqliteDbConfigOption.MAINDBNAME] is the only string that must
     * stay alive until the database connection is closed.
     */
    data class OfString(override val value: String, val key: String) : VariadicValue<Nothing>
}