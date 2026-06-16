@file:Suppress("SpellCheckingInspection")

package ksqlite.kapi.vtab

/**
 * Exposes Virtual Table configuration API.
 *
 * [Virtual Table Configuration Options](https://sqlite.org/c3ref/c_vtab_constraint_support.html)
 */
public interface VirtualTableConfiguration {

    /**
     * Sets whether the virtual table implementation guarantees that if xUpdate returns
     * SQLITE_CONSTRAINT, it will do so before any modifications to internal or persistent data
     * structures have been made.
     */
    public fun setConstraintSupportEnabled(enabled: Boolean)

    /**
     * Marks the virtual table as being safe to use from within triggers and views.
     */
    public fun setInnocuous()

    /**
     * Prohibits the use of the virtual table from within triggers and views.
     */
    public fun setDirectonly()

    /**
     * Instructs the query planner to begin at least a read transaction on all schemas ("main",
     * "temp", and any ATTACH-ed databases) whenever the virtual table is used.
     */
    public fun setUsesAllSchemas()
}