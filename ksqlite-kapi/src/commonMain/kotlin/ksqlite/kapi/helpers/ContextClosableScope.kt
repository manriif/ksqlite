package ksqlite.kapi.helpers

import ksqlite.capi.sqlite3_context

/**
 * Closable scope owning a [context].
 */
@PublishedApi
internal open class ContextClosableScope(
    @PublishedApi
    internal val context: sqlite3_context
): UnsafeClosableScope()