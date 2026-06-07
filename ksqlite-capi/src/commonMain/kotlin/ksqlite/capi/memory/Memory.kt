package ksqlite.capi.memory

internal typealias ConcurrentMap<K, V> = co.touchlab.stately.collections.ConcurrentMutableMap<K, V>
internal typealias Lock = co.touchlab.stately.concurrency.Lock

/**
 * Marker for object having a clearly defined lifecycle.
 */
public interface MemoryScope

/**
 * Clears all the resources owned by ksqlite.
 * It is recommended to close all opened sqlite database connections before calling that function.
 *
 * If all the resources have been correctly cleaned up before that method call, `true` is returned.
 * If `false` is returned then either some resource(s) have not been cleaned up correctly or
 * something escaped its owner (or both).
 */
public expect fun ksqliteCleanup(): Boolean