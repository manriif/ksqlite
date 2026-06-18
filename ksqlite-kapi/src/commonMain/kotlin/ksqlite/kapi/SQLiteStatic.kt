package ksqlite.kapi

/**
 * Exposes the SQLite API that does not require initialization.
 */
public interface SQLiteStatic {

    /**
     * Returns the options that were defined at compile-time.
     * The `SQLITE_` prefix is omitted for each option.
     */
    public val compileOptions: List<String>

    /**
     * Number of distinct keywords understood by SQLite.
     */
    public val keywordCount: Int

    /**
     * SQLite version.
     */
    public val version: String

    /**
     * SQLite version number.
     */
    public val versionNumber: Int

    /**
     * Identifier of the check-in of SQLite within its configuration management system.
     */
    public val sourceId: String

    /**
     * Returns `true` if `this` seems to form a complete SQL statement. If additional input is
     * needed before sending tbe text into SQLite for parsing, then `false` is returned.
     *
     * @throws SQLiteException if a memory allocation fails.
     */
    public fun isCompleteSqlStatement(sql: String): Boolean

    /**
     * Returns whether [word] is a keyword.
     */
    public fun isKeyword(word: String): Boolean

    /**
     * Returns the keyword at given [index].
     *
     * @throws SQLiteException if [index] is out of bounds.
     */
    public fun getKeyword(index: Int): String

    /**
     * Logs content using SQLite logging interface.
     */
    public fun log(
        errorCode: Int,
        message: String
    )
}