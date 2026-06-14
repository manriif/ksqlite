package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteCollationCompareCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.types.sqlite3
import ksqlite.foreign.callbacks.CollationCompareCallback
import ksqlite.foreign.callbacks.CollationNeededCallback
import ksqlite.types.internal.convertTextEncoding

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationCompareHandler<AppData> :
    Handler<SqliteCollationCompareCallback<AppData>, AppData>(),
    CollationCompareCallback {

    override fun apply(
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
    Handler<SqliteCollationNeededCallback<AppData>, AppData>(),
    CollationNeededCallback {

    override fun apply(
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