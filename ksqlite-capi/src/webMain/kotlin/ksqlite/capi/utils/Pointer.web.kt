package ksqlite.capi.utils

import ksqlite.capi.sqlite3
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmPointer

/**
 * Whether `this` [WasmPointer] points to a null pointer.
 */
internal val WasmPointer.isNull: Boolean
    get() = this == NullPtr

/**
 * Returns `null` if `this` [WasmPointer] points to a null pointer.
 */
internal val WasmPointer.orNull: WasmPointer?
    get() = takeUnless { isNull }

/**
 * Returns a non-null [WasmPointer].
 */
internal val WasmPointer?.notNull: WasmPointer
    get() = this ?: NullPtr

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun WasmPointer.toKStringFromUtf8(): String {
    return sqlite3.wasm.cstrToJs(this)
}

///////////////////////////////////////////////////////////////////////////
// Arrays
///////////////////////////////////////////////////////////////////////////
/*
/**
 * Returns an array of [count] item of type [T] obtained from [transform].
 */
internal inline fun <reified T> WasmPointer.toArray(
    count: Int,
    transform: (WasmPointer) -> T
): Array<T> {
    if (count == 0) {
        return emptyArray()
    }

    return Array(count) { transform(getAtIndex(ValueLayout.ADDRESS, it.toLong())) }
}

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a heap [WasmPointer] backed by the on-heap region of memory that holds the given byte
 * array.
 */
internal fun ByteArray.backing(): WasmPointer {
    return WasmPointer.ofArray(this)
}

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [WasmPointer.NULL] if `this` is `null`.
 */
context(allocator: WasmAllocator)
internal fun String?.allocateUtf8(): WasmPointer {
    return allocator.allocateFrom(this, Charsets.UTF_8)
}

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 */
context(allocator: WasmAllocator)
internal fun Array<String>?.allocateUtf8Array(): WasmPointer {
    if (this == null) {
        return WasmPointer.NULL
    }

    val pointers = allocator.allocate(ValueLayout.ADDRESS, size.toLong())

    forEachIndexed { index, string ->
        pointers.setAtIndex(ValueLayout.ADDRESS, index.toLong(), string.allocateUtf8())
    }

    return pointers
}

/**
 * Reads and returns a null terminated String starting from [offset].
 */
internal fun WasmPointer.getStringUtf8(offset: Long = 0): String {
    return checkNotNull(getString(offset, Charsets.UTF_8))
}

/**
 * Reads and returns a null terminated String starting from [offset] or returns `null` if `this`
 * [WasmPointer] is [WasmPointer.NULL].
 */
internal fun WasmPointer.getStringUtf8OrNull(offset: Long = 0): String? {
    if (isNull) {
        return null
    }

    return getStringUtf8(offset)
}*/