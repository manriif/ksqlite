@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.Capi
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_pointer

private val capi: Capi
    inline get() = sqlite3.capi

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_pointer? = capi.sqlite3_aggregate_context(
    context,
    nBytes
)