@file:Suppress("ClassName")

package ksqlite.capi.types

/**
 * The authorizer callback function must return either SQLITE_OK or one of these two constants in
 * order to signal SQLite whether or not the action is permitted.
 * See the [authorizer documentation](https://sqlite.org/c3ref/set_authorizer.html)
 * for additional information.
 * 
 * [Authorizer Return Codes](https://sqlite.org/c3ref/c_deny.html).
 */
public enum class Sqlite3AuthorizerCode(internal val code: Int) {

    /**
     * Same semantic and value as [Sqlite3Result.OK].
     */
    OK(Sqlite3Result.OK.code),

    /**
     * Abort the SQL statement with an error.
     */
    DENY(1),

    /**
     * Don't allow access, but don't generate an error.
     */
    IGNORE(2)
}