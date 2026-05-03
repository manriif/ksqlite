import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Name of the ksqlite extension.
 */
const val KSQLITE_EXTENSION_NAME = "ksqlite"

/**
 * Extension for [KsqlitePlugin].
 */
interface KsqliteExtension {

    /**
     * Directory where the additional ksqlite C related files are located.
     */
    val ksqliteDirectory: DirectoryProperty

    /**
     * Directory where the SQLite source tree is located.
     */
    val sqliteDirectory: DirectoryProperty

    /**
     * Name of the generated library.
     */
    val libraryName: Property<String>
}