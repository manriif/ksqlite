package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer

public actual class sqlite3_vtab internal constructor(pointer: Long) :
    StructPointer(pointer)