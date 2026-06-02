package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual class sqlite3_index_info internal constructor(pointer: Long) :
    Struct(pointer),
    Sqlite3IndexInfo