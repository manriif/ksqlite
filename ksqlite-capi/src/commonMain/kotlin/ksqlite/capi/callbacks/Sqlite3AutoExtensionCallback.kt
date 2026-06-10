package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3

/**
 * Callback to use with [ksqlite.capi.sqlite3_auto_extension].
 */
public fun interface Sqlite3AutoExtensionCallback {

    /**
     * Result for [apply].
     */
    public sealed interface Result

    /**
     * Scope for [apply].
     */
    public sealed interface Scope {

        /**
         * Returns [Sqlite3Result.OK] to SQLite.
         */
        public fun success(): Result

        /**
         * Returns a failure [result] to SQLite and writes [message] to `pzErrMsg`.
         */
        public fun failure(
            result: Sqlite3Result.Failure,
            message: String
        ): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/auto_extension.html).
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope.apply(db: sqlite3): Result
}