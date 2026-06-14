package ksqlite.capi.callbacks

import ksqlite.types.SqliteActionCode
import ksqlite.types.SqliteAuthorizerCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_set_authorizer].
 */
public fun interface SqliteAuthorizerCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/set_authorizer.html).
     */
    public fun apply(
        appData: AppData,
        action: SqliteActionCode,
        param3: String?,
        param4: String?,
        param5: String?,
        param6: String?
    ): SqliteAuthorizerCode
}