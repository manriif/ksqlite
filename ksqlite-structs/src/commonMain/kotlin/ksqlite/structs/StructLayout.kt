@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.structs

/**
 * Layout of a struct.
 */
public typealias StructLayout = IntArray

/**
 * Provides the layout of a given struct type.
 */
public fun interface StructLayoutProvider {

    /**
     * Returns the layout for the struct identified by [type].
     */
    public fun provide(type: RawStructType): StructLayout
}

///////////////////////////////////////////////////////////////////////////
// Instance
///////////////////////////////////////////////////////////////////////////

/**
 * Global [StructLayoutProvider].
 */
private var StructLayoutProviderInstance: StructLayoutProvider? = null

/**
 * Returns the global [StructLayoutProvider].
 */
internal val structLayoutProvider: StructLayoutProvider
    get() = checkNotNull(StructLayoutProviderInstance) {
        "StructLayoutProvider was not set"
    }

/**
 * Sets the [provider] that is used globally.
 */
public fun setStructLayoutProvider(provider: StructLayoutProvider) {
    StructLayoutProviderInstance = provider
}

///////////////////////////////////////////////////////////////////////////
// Layout
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the whole struct size in bytes.
 */
internal inline val StructLayout.structSize: Int
    get() = last()

/**
 * Returns the offset of the member at [index].
 */
internal inline fun StructLayout.memberOffset(index: Int) = get(index * 2)

/**
 * Returns the size in bytes of the member at [index].
 */
internal inline fun StructLayout.memberSize(index: Int) = get(index * 2 + 1)
