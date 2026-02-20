@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite.types

import cnames.structs.sqlite3
import cnames.structs.sqlite3_context
import cnames.structs.sqlite3_stmt
import cnames.structs.sqlite3_value
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import ksqlite.memory.MemoryManager

///////////////////////////////////////////////////////////////////////////
// Generic
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3_pointer(internal val pointer: COpaquePointer)

///////////////////////////////////////////////////////////////////////////
// Sqlite
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3(internal val pointer: CPointer<sqlite3>) : MemoryManager()

public actual class sqlite3_context(internal val pointer: CPointer<sqlite3_context>)

public actual class sqlite3_stmt(internal val pointer: CPointer<sqlite3_stmt>) : MemoryManager()

public actual class sqlite3_value(internal val pointer: CPointer<sqlite3_value>)