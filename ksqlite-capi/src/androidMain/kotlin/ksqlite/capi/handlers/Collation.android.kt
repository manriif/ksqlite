package ksqlite.capi.handlers

import ksqlite.CollationNeededCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
internal class CollationNeededHandler<AppData> :
    Handler<Sqlite3CollationNeededCallback<AppData>, AppData>(),
    CollationNeededCallback {

    override fun call(
        db: Long,
        eTextRep: Int,
        name: String
    ) = handler { callback, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name
        )
    }
}