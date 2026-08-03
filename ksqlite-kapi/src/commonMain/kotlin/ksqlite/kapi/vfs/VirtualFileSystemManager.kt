package ksqlite.kapi.vfs

/**
 * Manages the [VirtualFileSystemBase]s.
 */
public interface VirtualFileSystemManager {

    /**
     * Returns the default virtual file system.
     */
    public val default: VirtualFileSystemBase?

    /**
     * Finds the [VirtualFileSystemBase] for the given [name]. A `null` [name] can be supplied
     * to query the default virtual file system.
     */
    public fun find(name: String): VirtualFileSystemBase?

    /**
     * Registers this given [vfs]. If [makeDefault] is `true`, then it is set as the default  one.
     *
     * @throws ksqlite.kapi.SQLiteException if registering the virtual file system fails.
     */
    public fun register(
        vfs: VirtualFileSystemBase,
        makeDefault: Boolean
    )

    /**
     * Unregisters the given [vfs].
     *
     * @throws ksqlite.kapi.SQLiteException if unregistering the virtual file system fails.
     */
    public fun unregister(vfs: VirtualFileSystemBase)
}