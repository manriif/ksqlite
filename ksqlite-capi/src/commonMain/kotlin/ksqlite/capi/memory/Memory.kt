package ksqlite.capi.memory

internal typealias ConcurrentMap<K, V> = co.touchlab.stately.collections.ConcurrentMutableMap<K, V>
internal typealias Lock = co.touchlab.stately.concurrency.Lock

/**
 * Marker for object having a clearly defined lifecycle.
 */
public interface MemoryScope