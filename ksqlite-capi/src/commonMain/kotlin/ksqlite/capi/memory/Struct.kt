package ksqlite.capi.memory

/**
 * Base for [Struct].
 */
public abstract class StructBase internal constructor() {

    internal abstract val address: Long

    override fun toString(): String =
        "${this::class.simpleName}(address=0x${address.toHexString()})"
}

/**
 * Represents a native struct object.
 * Direct inheritor of [Struct] cannot be allocated and only holds an opaque pointer.
 *
 * Two [Struct]s representing the same native object are structurally equals (==).
 */
public expect open class Struct : StructBase {

    override val address: Long
}

/**
 * [Struct] that has been allocated by the application.
 *
 * If the [AllocatedStruct] was obtained by invoking one of its constructor or factory function,
 * then it is owned by the application. Otherwise, the struct is owned by SQLite.
 *
 * The instantiator, which is the owner of the [AllocatedStruct], is responsible for closing it.
 */
public expect open class AllocatedStruct :
    Struct,
    AutoCloseable {

    /**
     * Frees the struct if it was allocated by the application.
     * Does nothing if the struct was not allocated by the application.
     *
     * /!\ If the method is overloaded, super.[close] must be called.
     */
    override fun close()
}