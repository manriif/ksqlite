package ksqlite.types.vfs

/**
 * Describes an [`sqlite3_vfs`](https://sqlite.org/c3ref/vfs.html) struct.
 */
public interface SqliteVfs {

    /**
     * Structure version number.
     */
    public val iVersion: SqliteVfsVersion

    /**
     * Size of subclassed sqlite3_file.
     */
    public val szOsFile: Int

    /**
     * Maximum file pathname length.
     */
    public val mxPathname: Int

    /**
     * Name of this virtual file system.
     */
    public val zName: String
}