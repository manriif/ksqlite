package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.StructPointer

public actual class sqlite3_index_info internal constructor(override val pointer: CPointer<s3_index_info>) :
    StructPointer(pointer),
    Sqlite3IndexInfo