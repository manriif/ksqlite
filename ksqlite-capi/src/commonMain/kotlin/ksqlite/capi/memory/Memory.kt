package ksqlite.capi.memory

internal typealias ConcurrentMap<K, V> = co.touchlab.stately.collections.ConcurrentMutableMap<K, V>
internal typealias Lock = co.touchlab.stately.concurrency.Lock

/**
 * Marker for object having a clearly defined lifecycle.
 */
public interface MemoryScope

/**
 * Hex format for native address printing.
 */
internal val NativeAddressHexFormat = HexFormat {
    number {
        removeLeadingZeros = true
    }
}