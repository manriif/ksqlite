@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.StructPointer

/**
 * An instance of the snapshot object records the state of a WAL mode database for some specific
 * point in history.
 *
 * [sqlite3_snapshot](https://sqlite.org/c3ref/snapshot.html)
 */
public expect class sqlite3_snapshot : StructPointer