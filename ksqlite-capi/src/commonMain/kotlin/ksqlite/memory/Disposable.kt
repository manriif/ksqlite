package ksqlite.memory

/**
 * Resource which can be disposed.
 */
internal interface Disposable {

    /**
     * Disposes the resource.
     */
    fun dispose()
}