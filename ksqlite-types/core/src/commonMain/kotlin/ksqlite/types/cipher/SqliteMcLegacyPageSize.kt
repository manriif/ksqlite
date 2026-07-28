package ksqlite.types.cipher

/**
 * Allowed values for legacy page size cipher parameters.
 */
public enum class SqliteMcLegacyPageSize(override val value: Int) :
    SqliteMcConfigParam.IntRepresentable {

    /**
     * Default SQLite page size.
     */
    DEFAULT(0),
    BYTES_128(128),
    BYTES_256(256),
    BYTES_512(512),
    BYTES_1024(1024),
    BYTES_2048(2048),
    BYTES_4096(4096),
    BYTES_8192(8192),
    BYTES_16384(16384),
    BYTES_32768(32768),
    BYTES_65536(65536);

    ///////////////////////////////////////////////////////////////////////////
    // Param
    ///////////////////////////////////////////////////////////////////////////

    public open class Param(name: String = "legacy_page_size") :
        SqliteMcConfigParam.OfEnum<SqliteMcLegacyPageSize>(name, entries)
}