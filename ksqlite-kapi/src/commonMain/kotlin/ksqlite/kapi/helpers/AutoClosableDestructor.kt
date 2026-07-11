package ksqlite.kapi.helpers

import ksqlite.capi.callbacks.SqliteDestroyCallback

/**
 * Destructor invoking [AutoCloseable.close] on the argument it receives.
 */
internal val AutoCloser = SqliteDestroyCallback{ value: AutoCloseable ->
    value.close()
}

/**
 * Returns [AutoCloser] if the [value] is an instance of [AutoCloseable].
 */
@PublishedApi
internal fun autoCloser(value: Any): SqliteDestroyCallback<Any>? {
    if (value is AutoCloseable) {
        @Suppress("UNCHECKED_CAST")
        return AutoCloser as SqliteDestroyCallback<Any>
    }

    return null
}