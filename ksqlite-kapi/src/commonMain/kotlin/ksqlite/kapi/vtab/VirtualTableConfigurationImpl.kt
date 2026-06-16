package ksqlite.kapi.vtab

import ksqlite.capi.sqlite3_vtab_config
import ksqlite.capi.types.SqliteVTabConfigOption
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.helpers.ClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck

internal class VirtualTableConfigurationImpl(
    private val db: sqlite3,
    private val scope: ClosableScope
) : VirtualTableConfiguration {

    /**
     * Applies the given configuration [option].
     */
    private fun applyOption(option: SqliteVTabConfigOption) =
        scope.notClosed { sqliteResultCheck(sqlite3_vtab_config(db, option)) }

    /**
     * Sets whether the virtual table implementation guarantees that if xUpdate returns
     * SQLITE_CONSTRAINT, it will do so before any modifications to internal or persistent data
     * structures have been made.
     */
    override fun setConstraintSupportEnabled(enabled: Boolean) =
        applyOption(SqliteVTabConfigOption.CONSTRAINT_SUPPORT(if (enabled) 1 else 0))

    /**
     * Marks the virtual table as being safe to use from within triggers and views.
     */
    override fun setInnocuous() =
        applyOption(SqliteVTabConfigOption.INNOCUOUS)

    /**
     * Prohibits the use of the virtual table from within triggers and views.
     */
    override fun setDirectonly() =
        applyOption(SqliteVTabConfigOption.DIRECTONLY)

    /**
     * Instructs the query planner to begin at least a read transaction on all schemas ("main",
     * "temp", and any ATTACH-ed databases) whenever the virtual table is used.
     */
    override fun setUsesAllSchemas() =
        applyOption(SqliteVTabConfigOption.USES_ALL_SCHEMAS)
}