package ksqlite.kapi.helpers

import ksqlite.capi.types.sqlite3_context

/**
 * Closable scope owning a [context].
 */
@PublishedApi
internal open class ContextClosableScope(
    @PublishedApi
    internal val context: sqlite3_context
): ClosableScope()