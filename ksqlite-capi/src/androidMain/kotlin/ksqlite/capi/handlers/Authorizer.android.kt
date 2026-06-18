package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.foreign.callbacks.AuthorizerCallback
import ksqlite.types.internal.convertActionCode

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class AuthorizerHandler<AppData> :
    Handler<SqliteAuthorizerCallback<AppData>, AppData>(),
    AuthorizerCallback {

    override fun apply(
        opId: Int,
        string1: String?,
        string2: String?,
        string3: String?,
        string4: String?
    ): Int = handle { callback, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(opId),
            detail1 = string1,
            detail2 = string2,
            detail3 = string3,
            detail4 = string4
        ).code
    }
}