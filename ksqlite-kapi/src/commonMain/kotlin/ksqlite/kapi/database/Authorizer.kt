package ksqlite.kapi.database

import ksqlite.types.SqliteActionCode
import ksqlite.types.SqliteAuthorizerStatus

/**
 * Callback to use with [DatabaseConnection.setAuthorizer].
 */
public fun interface Authorizer {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/set_authorizer.html).
     */
    public fun apply(
        action: SqliteActionCode,
        detail1: String?,
        detail2: String?,
        detail3: String?,
        detail4: String?
    ): SqliteAuthorizerStatus
}