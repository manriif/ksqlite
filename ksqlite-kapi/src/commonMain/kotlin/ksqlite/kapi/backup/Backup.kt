package ksqlite.kapi.backup

import ksqlite.kapi.MAIN_DB_NAME
import ksqlite.kapi.connection.Connection

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
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun step(count: Int)

    /**
     * Releases all resources associated with `this` backup.
     *
     * @throws ksqlite.kapi.SQLiteException if finalizing the backup fails.
     */
    override fun close()

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides methods to initializes a [Backup].
     */
    public companion object {

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
        ): Backup = createBackup(
            destination = destination,
            destinationName = destinationName,
            source = source,
            sourceName = sourceName
        )

        /**
         * Initializes a backup, copying content from database `main` using the connection [source]
         * into the database `main` using connection [destination] and returns a [Backup].
         *
         * The returned [Backup] must be closed when done with.
         */
        public fun init(
            destination: Connection,
            source: Connection
        ): Backup = createBackup(
            destination = destination,
            destinationName = MAIN_DB_NAME,
            source = source,
            sourceName = MAIN_DB_NAME
        )
    }
}