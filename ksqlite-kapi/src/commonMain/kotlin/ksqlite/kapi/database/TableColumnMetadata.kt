package ksqlite.kapi.database

/**
 * Result of [DatabaseConnection.tableColumnMetadata].
 * Member fields are all already resolved.
 */
public interface TableColumnMetadata {

    /**
     * Column data type.
     */
    public val dataType: String

    /**
     * Name of the default collation sequence.
     */
    public val collationSequence: String

    /**
     * Whether the column has NOT NULL constraint.
     */
    public val isNullable: Boolean

    /**
     * Whether the column is part of the PRIMARY KEY.
     */
    public val isPrimaryKey: Boolean

    /**
     * Whether the column is AUTOINCREMENT.
     */
    public val isAutoIncrement: Boolean
}