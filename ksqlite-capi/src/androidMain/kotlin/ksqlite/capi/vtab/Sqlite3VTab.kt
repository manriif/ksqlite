package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual class sqlite3_vtab internal constructor(pointer: Long) :
    Struct(pointer)