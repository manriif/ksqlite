package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual class sqlite3_module internal constructor(pointer: Long) :
    Struct(pointer)