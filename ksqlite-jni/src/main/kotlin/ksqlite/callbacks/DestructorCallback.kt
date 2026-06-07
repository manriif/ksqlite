package ksqlite.callbacks

/**
 * Application data destructor.
 */
public fun interface DestructorCallback {

    /**
     * Invoked from JNI.
     */
    public fun apply()
}