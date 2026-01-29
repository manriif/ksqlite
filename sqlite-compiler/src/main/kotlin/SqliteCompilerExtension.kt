import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Extension for [SqliteCompilerPlugin].
 */
abstract class SqliteCompilerExtension @Inject constructor() {

    /**
     * Directory where the Android NDK resides.
     */
    abstract val androidNdkDirectory: DirectoryProperty

    /**
     * Minimum Android SDK.
     */
    abstract val androidSdkMin: Property<String>

    /**
     * Minimum macOS version.
     */
    abstract val macosVersionMin: Property<String>

    /**
     * Minimum iOS version.
     */
    abstract val iosVersionMin: Property<String>

    /**
     * Minimum tvOS version.
     */
    abstract val tvosVersionMin: Property<String>

    /**
     * Minimum watchOS version.
     */
    abstract val watchosVersionMin: Property<String>

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
}