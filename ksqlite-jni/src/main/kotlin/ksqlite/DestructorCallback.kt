package ksqlite

/**
 * User data destructor.
 */
public fun interface DestructorCallback {

    /**
     * Invoked from JNI.
     */
    public fun destroy()
}