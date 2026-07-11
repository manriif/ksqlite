package ksqlite.types

public sealed interface SqliteFunctionTextEncoding {

    public val value: Int

    /**
     * Returns an [SqliteTextEncoding] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteFunctionFlag): SqliteFunctionTextEncoding =
        Mask(value or flag.value)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * [SqliteTextEncoding] applying a mask and that can be used with create functions routines.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteFunctionTextEncoding
}