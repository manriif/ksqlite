package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.setCommitHook].
 */
public fun interface CommitHook {

    /**
     * Returns `true` to convert the commit into a rollback or `false` to let the operation continue
     * normally.
     *
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/commit_hook.html).
     */
    public fun apply(): Boolean
}