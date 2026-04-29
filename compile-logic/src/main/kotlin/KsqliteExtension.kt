import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.getByName

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

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Name of the ksqlite extension.
 */
const val KSQLITE_EXTENSION_NAME = "ksqlite"

/**
 * Retrieves and returns the [KsqliteExtension] from root project.
 */
val Project.ksqliteExtension: KsqliteExtension
    get() = rootProject.extensions.getByName<KsqliteExtension>(
        KSQLITE_EXTENSION_NAME
    )