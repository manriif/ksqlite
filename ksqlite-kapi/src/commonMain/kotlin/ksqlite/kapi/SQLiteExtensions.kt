package ksqlite.kapi

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.buffer.readBytes

/**
 * Returns a [ByteArray] of given [size] filled with random bytes.
 */
public fun SQLite.generateRandomBytes(size: Int): ByteArray {
    val output = Buffer.allocate(size)
    generateRandomBytes(output, size)
    return output.readBytes()
}