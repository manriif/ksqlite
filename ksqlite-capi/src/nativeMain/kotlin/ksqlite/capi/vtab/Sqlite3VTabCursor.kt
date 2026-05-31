package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.StructPointer

public actual class sqlite3_vtab_cursor internal constructor(override val pointer: CPointer<s3_vtab_cursor>) :
    StructPointer(pointer)