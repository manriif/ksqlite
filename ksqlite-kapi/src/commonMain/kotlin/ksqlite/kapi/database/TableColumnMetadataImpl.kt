package ksqlite.kapi.database

internal data class TableColumnMetadataImpl(
    override val dataType: String,
    override val collationSequence: String,
    override val isNullable: Boolean,
    override val isPrimaryKey: Boolean,
    override val isAutoIncrement: Boolean
) : TableColumnMetadata