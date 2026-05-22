package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3ActionCode
import ksqlite.capi.types.Sqlite3AuthorizerCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_set_authorizer].
 */
public fun interface Sqlite3SetAuthorizerCallback <ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/set_authorizer.html).
     */
    public fun handle(
        clientData: ClientData,
        action: Sqlite3ActionCode,
        param3: String?,
        param4: String?,
        param5: String?,
        param6: String?
    ): Sqlite3AuthorizerCode
}