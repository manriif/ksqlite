package ksqlite.kapi.database

/**
 * Callback to use with [DatabaseConnection.execute].
 */
public fun interface Exec {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/exec.html).
     *
     * The application must not access more than [columnCount] elements from [columnValues] and
     * [columnNames] even if they contain more than [columnCount] elements.
     *
     * To abort the execution, `true` must be returned, `false` to keep it continue.
     */
    public fun apply(
        columnCount: Int,
        columnValues: Array<String?>,
        columnNames: Array<String>
    ): Boolean
}