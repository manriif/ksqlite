package ksqlite.types

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import ksqlite.utils.checkRange

public actual class Sqlite3Buffer(
    private val nativeBuffer: CPointer<ByteVar>,
    public actual val nativeSize: Int
) {

    public actual fun read(
        sourceOffset: Int,
        destinationOffset: Int,
        size: Int,
        destination: ByteArray
    ): ByteArray {
        checkRange(
            sourceOffset = sourceOffset,
            sourceSize = nativeSize,
            destinationOffset = destinationOffset,
            destinationSize = destination.size,
            size = size
        )

        repeat(size) { index ->
            destination[index + destinationOffset] = nativeBuffer[index + sourceOffset]
        }

        return destination
    }

    public actual fun write(
        source: ByteArray,
        sourceOffset: Int,
        destinationOffset: Int,
        size: Int
    ) {
        checkRange(
            sourceOffset = sourceOffset,
            sourceSize = source.size,
            destinationOffset = destinationOffset,
            destinationSize = nativeSize,
            size = size
        )

        repeat(size) { index ->
            nativeBuffer[index + destinationOffset] = source[index + sourceOffset]
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an [Sqlite3Buffer] or `null` if [pointer] is `null`.
 */
internal fun createBuffer(pointer: COpaquePointer?, size: Int): Sqlite3Buffer? {
    if (pointer == null) {
        return null
    }

    return Sqlite3Buffer(
        nativeBuffer = pointer.reinterpret(),
        nativeSize = size
    )
}