package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer

public actual class sqlite3_index_info internal constructor(pointer: Long) :
    StructPointer(pointer),
    Sqlite3IndexInfo