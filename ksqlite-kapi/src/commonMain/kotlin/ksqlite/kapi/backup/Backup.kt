package ksqlite.kapi.backup

import ksqlite.capi.sqlite3_backup_init
import ksqlite.capi.sqlite3_errcode
import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.types.Sqlite3Result
import ksqlite.kapi.connection.Connection
import ksqlite.kapi.throwSQLiteException

/**
 * Exposes the [Online Backup API](https://sqlite.org/backup.html).
 */
public interface Backup : AutoCloseable {

    /**
     * Returns the total number of pages in the source database as of the most recent call to [step].
     */
    public val pageCount: Int

    /**
     * Returns the number of pages still to be backed up as of the most recent call to [step].
     */
    public val remaining: Int

    /**
     * Copies up to [count] pages between the source and destination databases.
     */
    public fun step(count: Int)

    /**
     * Releases all resources associated with `this` backup.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides methods to initializes a [Backup].
     */
    public companion object {

        private const val MAIN_DB_NAME = "main"

        /**
         * Initializes a backup, copying content from database [sourceName] using the connection
         * [source] into the database [destinationName] using connection [destination] and returns
         * a [Backup].
         *
         * The returned [Backup] must be closed when done with.
         */
        public fun init(
            destination: Connection,
            destinationName: String,
            source: Connection,
            sourceName: String
        ): Backup {
            val backup = sqlite3_backup_init(destination.db, destinationName, source.db, sourceName)

            if (backup == null) {
                val message = sqlite3_errmsg(destination.db) ?: "Failed to initializes a backup"
                val result = sqlite3_errcode(destination.db)

                check(result is Sqlite3Result.Failure) {
                    "Unexpected result $result after a sqlite3_backup_init() failure"
                }

                throwSQLiteException(message, result)
            }

            return BackupImpl(backup)
        }

        /**
         * Initializes a backup, copying content from database `main` using the connection [source] into the
         * database `main` using connection [destination] and returns a [Backup].
         *
         * The returned [Backup] must be closed when done with.
         */
        public fun init(
            destination: Connection,
            source: Connection
        ): Backup = init(
            destination = destination,
            destinationName = MAIN_DB_NAME,
            source = source,
            sourceName = MAIN_DB_NAME
        )
    }
}