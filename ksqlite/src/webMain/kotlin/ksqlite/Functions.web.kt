@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

import sqlite.Capi

private val capi: Capi
    inline get() = sqlite.sqlite3.capi

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): pointer? = capi.sqlite3_aggregate_context(
    context,
    nBytes
)