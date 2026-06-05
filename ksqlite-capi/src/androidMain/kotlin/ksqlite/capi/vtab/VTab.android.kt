package ksqlite.capi.vtab

import ksqlite.OutputPointer
import ksqlite.callbacks.VTabModuleCallbacks as JniVTabModuleCallbacks

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = ksqlite.structs.sqlite3_index_info
internal typealias s3_module = ksqlite.structs.sqlite3_module
internal typealias s3_vtab =ksqlite.structs. sqlite3_vtab
internal typealias s3_vtab_cursor = ksqlite.structs.sqlite3_vtab_cursor

///////////////////////////////////////////////////////////////////////////
// Handlers
///////////////////////////////////////////////////////////////////////////

/**
 * Handles all callbacks of a Virtual Table module.
 */
internal class VTabModuleHandler: JniVTabModuleCallbacks {

    override fun create(
        db: Long,
        argv: Array<String>,
        ppVtab: OutputPointer.OfPointer,
        pzErrMsg: OutputPointer.OfPointer
    ): Int {
        TODO("Not yet implemented")
    }

    override fun connect(
        db: Long,
        argv: Array<String>,
        ppVtab: OutputPointer.OfPointer,
        pzErrMsg: OutputPointer.OfPointer
    ) {
        TODO("Not yet implemented")
    }
}