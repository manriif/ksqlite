package ksqlite.capi.handlers

import ksqlite.AuthorizerCallback
import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback
import ksqlite.capi.convertActionCode

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class AuthorizerHandler<AppData> :
    Handler<Sqlite3AuthorizerCallback<AppData>, AppData>(),
    AuthorizerCallback {

    override fun call(
        opId: Int,
        string1: String?,
        string2: String?,
        string3: String?,
        string4: String?
    ): Int = handler { callback, appData ->
        callback.handle(
            appData = appData,
            action = convertActionCode(opId),
            param3 = string1,
            param4 = string2,
            param5 = string3,
            param6 = string4
        ).code
    }
}