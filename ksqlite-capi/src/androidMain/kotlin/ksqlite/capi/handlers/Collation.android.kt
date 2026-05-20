package ksqlite.capi.handlers

import ksqlite.CollationNeededCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.types.Sqlite3CollationNeededCallback
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
internal class CollationNeededHandler(holder: Holder<Sqlite3CollationNeededCallback>) :
    Handler<Sqlite3CollationNeededCallback>(holder),
    CollationNeededCallback {

    override fun call(
        db: Long,
        eTextRep: Int,
        name: String
    ) = handler { callback, userData ->
        callback(
            userData,
            sqlite3(db),
            convertTextEncoding(eTextRep),
            name
        )
    }
}