package ksqlite.kapi.vtab

/**
 * Optional functions of a [VirtualTable].
 */
public enum class VirtualTableOptionalFunction {
    /**
     * Represents [VirtualTable.update].
     */
    Update,

    /**
     * Represents [VirtualTable.findFunction].
     */
    FindFunction,

    /**
     * Represents [VirtualTable.begin].
     */
    Begin,

    /**
     * Represents [VirtualTable.sync].
     */
    Sync,

    /**
     * Represents [VirtualTable.commit].
     */
    Commit,

    /**
     * Represents [VirtualTable.rollback].
     */
    Rollback,

    /**
     * Represents [VirtualTable.rename].
     */
    Rename,

    /**
     * Represents [VirtualTable.savepoint].
     */
    Savepoint,

    /**
     * Represents [VirtualTable.release].
     */
    Release,

    /**
     * Represents [VirtualTable.rollbackTo].
     */
    RollbackTo,

    /**
     * Represents [VirtualTable.integrity].
     */
    Integrity
}