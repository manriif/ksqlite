package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.execute].
 */
public fun interface Exec {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/exec.html).
     *
     * If the execution must be aborted, [abort] must be called.
     *
     * The application must not access more than [columnCount] elements from [columnValues] and
     * [columnNames] even if they contain more than [columnCount] elements.
     */
    public fun ExecScope.apply(
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    )
}