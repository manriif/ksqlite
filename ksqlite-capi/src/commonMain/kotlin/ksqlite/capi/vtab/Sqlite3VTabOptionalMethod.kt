package ksqlite.capi.vtab

/**
 * Method of [Sqlite3Module] that are optional.
 */
public enum class Sqlite3VTabOptionalMethod {
    UPDATE,
    BEGIN,
    SYNC,
    COMMIT,
    ROLLBACK,
    RENAME,
    SAVEPOINT,
    RELEASE,
    ROLLBACK_TO,
    SHADOW_NAME,
    INTEGRITY
}