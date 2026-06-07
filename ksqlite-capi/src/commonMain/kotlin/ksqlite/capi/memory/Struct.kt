package ksqlite.capi.memory

/**
 * Base for [Struct].
 */
public abstract class StructBase internal constructor() {

    internal abstract val address: Long

    override fun toString(): String =
        "${this::class.simpleName}(address=0x${address.toHexString()})"

    /**
     * Frees the struct releasing associated memory if managed by the VM.
     */
    internal abstract fun free()
}

/**
 * Wrapper around native struct.
 */
public expect open class Struct : StructBase {

    override val address: Long

    override fun free()
}