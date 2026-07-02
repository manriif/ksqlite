package ksqlite.capi.memory

/**
 * Returns [layoutSize] or [requestedSize] if it is greater than or equals to [layoutSize].
 */
internal fun checkStructSize(
    layoutSize: Long,
    requestedSize: Long?
): Long {
    val retainedSize = requestedSize ?: layoutSize

    check(retainedSize >= layoutSize) {
        "Requested struct size must not be less than the struct layout size"
    }

    return retainedSize
}