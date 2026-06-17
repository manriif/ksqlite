package ksqlite.kapi.database

/**
 * Special exception emitted to abort SQL statement execution.
 */
internal class ExecAbortException : Exception()

internal object ExecScopeImpl : ExecScope {

    override fun abort(): Nothing {
        throw ExecAbortException()
    }
}