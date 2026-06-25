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
 * Represents a native struct object.
 *
 * Two [Struct]s representing the same native object are structurally equals (==).
 */
public expect open class Struct : StructBase {

    override val address: Long

    override fun free()
}