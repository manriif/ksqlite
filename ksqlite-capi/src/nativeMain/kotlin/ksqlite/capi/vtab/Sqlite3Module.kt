package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.StructPointer

public actual class sqlite3_module internal constructor(override val pointer: CPointer<s3_module>) :
    StructPointer(pointer)