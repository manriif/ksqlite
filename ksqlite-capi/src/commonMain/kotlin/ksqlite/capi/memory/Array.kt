package ksqlite.capi.memory

/**
 * Base for all [StructLayout] implementations.
 */
public abstract class StructLayoutBase<S : ClosableStruct> {

    /**
     * Cleanups the given [instance].
     */
    internal abstract fun cleanup(instance: S)
}

public expect abstract class StructLayout<S : ClosableStruct> : StructLayoutBase<S>

/**
 * Base for all [StructArray] implementations.
 */
public abstract class StructArrayBase<S : ClosableStruct> internal constructor(
    private val layout: StructLayout<S>,
    private val elements: List<S>
) : AutoCloseable,
    Iterable<S> {

    /**
     * Returns the number of elements in this array.
     */
    public val size: Int
        get() = elements.size

    /**
     * Returns the element at [index].
     */
    public operator fun get(index: Int): S = elements[index]

    /**
     * Returns an iterator over [S]s.
     */
    public override fun iterator(): Iterator<S> = elements.iterator()

    /**
     * Releases the native array.
     */
    internal abstract fun releaseNativeArray()

    /**
     * Releases the native array and all its elements.
     */
    override fun close() {
        elements.forEach(layout::cleanup)
        releaseNativeArray()
    }
}

/**
 * Contiguous native array of structs (`S[]` / `const S*`)
 * It is not recommended to close an individual element.
 */
public expect class StructArray<S : ClosableStruct> : StructArrayBase<S> {
    override fun releaseNativeArray()
}

/**
 * Allocates a contiguous array, composed of [count] element of type [S].
 *
 * The allocated memory isn't guaranteed to be zeroized, so it is important to [initialize] all the
 * members of a given instance [S].
 *
 * If the allocation fails because not enough memory is available, then null is returned instead.
 */
public expect fun <S : ClosableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>?