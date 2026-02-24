package ksqlite.memory

/**
 * Reference to an object preventing GC from collecting or moving it.
 */
internal interface Reference {

    /**
     * Returns the object instance.
     */
    fun <Data : Any> get(): Data

    /**
     * Releases the object making it available to GC.
     */
    fun release()
}