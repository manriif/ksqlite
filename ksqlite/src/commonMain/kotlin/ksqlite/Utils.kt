@file:Suppress("NOTHING_TO_INLINE")

package ksqlite

/**
 * Ensures that [sourceOffset], [destinationOffset] and [size] are not negative and that content can
 * fit in source and destination.
 */
internal inline fun checkRange(
    sourceOffset: Int,
    sourceSize: Int,
    destinationOffset: Int,
    destinationSize: Int,
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

    require((sourceOffset + size) <= sourceSize) {
        "source buffer cannot provides the requested number of bytes"
    }

    require((destinationOffset + size) <= destinationSize) {
        "destination buffer cannot receives the requested number of bytes"
    }
}