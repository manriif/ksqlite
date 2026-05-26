package ksqlite.capi.memory

/**
 * Android memory is managed on C++ side (JNI).
 */
internal actual class MemoryManager : MemoryManagerBase()