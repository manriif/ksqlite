package ksqlite.capi.handlers

import ksqlite.CollationNeededCallback
import ksqlite.CollationCompareCallback
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationCompareHandler<AppData> :
    Handler<Sqlite3CollationCompareCallback<AppData>, AppData>(),
    CollationCompareCallback {

    override fun call(
        lhs: ByteArray,
        rhs: ByteArray
    ): Int = handle { callback, appData ->
        callback.apply(
            appData = appData,
            lhs = lhs,
            rhs = rhs
        )
    }
}

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
    ) = handle { callback, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name
        )
    }
}