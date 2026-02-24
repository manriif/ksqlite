@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.utils

/**
 * Ensures that [sourceOffset], [destinationOffset] and [size] are not negative and that content can
 * fit in source and destination.
 */
internal inline fun checkBufferRange(
    sourceOffset: Long,
    sourceSize: Long,
    destinationOffset: Long,
    destinationSize: Long,
    size: Int
) {
    require(sourceOffset >= 0) {
        "sourceOffset must not be negative ($sourceOffset)"
    }

    require(destinationOffset >= 0) {
        "destinationOffset must not be negative ($destinationOffset)"
    }

    require(size >= 0) {
        "size must not be negative ($size)"
    }

    require((sourceSize - sourceOffset) >= size) {
        "source buffer cannot provides the requested number of bytes"
    }

    require((destinationSize - destinationOffset) >= size) {
        "destination buffer cannot receives the requested number of bytes"
    }
}