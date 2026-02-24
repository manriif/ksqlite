@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

import ksqlite.types.sqlite3_context
import ksqlite.types.sqlite3_pointer
import sqlite.Capi

private val capi: Capi
    inline get() = sqlite.sqlite3.capi

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_pointer? = capi.sqlite3_aggregate_context(
    context,
    nBytes
)