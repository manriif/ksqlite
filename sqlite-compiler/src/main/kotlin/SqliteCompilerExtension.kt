import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Extension for [SqliteCompilerPlugin].
 */
abstract class SqliteCompilerExtension @Inject constructor() {

    /**
     * Release of SQLite that should be downloaded and compiled.
     */
    abstract val sqliteRelease: Property<SqliteRelease>

    /**
     * Directory where the SQLite source tree is downloaded and unzipped.
     */
    abstract val sqliteDownloadDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is stored.
     */
    abstract val sqliteSourcesDirectory: DirectoryProperty

    /**
     * Directory where the native SQLite artifacts are placed.
     */
    abstract val sqliteNativeLibDirectory: DirectoryProperty

    /**
     * Directory where the Android NDK resides.
     */
    abstract val androidNdkDirectory: DirectoryProperty

    /**
     * Task responsible for outputting the SQLite sources.
     */
    abstract val sourceTask: Property<Task>
}