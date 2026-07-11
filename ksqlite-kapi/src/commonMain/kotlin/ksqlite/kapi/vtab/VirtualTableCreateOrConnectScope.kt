package ksqlite.kapi.vtab


/**
 * Scope to use with [VirtualTableModule.connect] and [VirtualTableModule.Regular.create].
 */
public interface VirtualTableCreateOrConnectScope {

    /**
     * Configuration of the virtual table.
     */
    public val config: VirtualTableConfiguration

    /**
     * Declares the schema of the virtual table.
     */
    public fun declare(sql: String)

    /**
     * Declares that the virtual table overloads the function identified by [name] and
     * [argumentCount].
     *
     * The virtual table must implement [VirtualTable.findFunction].
     */
    public fun overloadFunction(
        name: String,
        argumentCount: Int
    )
}