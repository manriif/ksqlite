package ksqlite.capi.utils

/**
 * Returns the [transform]ed [block]'s result. If [block] returns `null` then `null` is returned.
 */
internal inline fun <T : Any, R> transform(
    transform: (T) -> R,
    block: () -> T?
): R? = transform(block() ?: return null)