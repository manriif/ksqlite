package compilation

import toolchains.Toolchains
import java.io.Serializable

/**
 * Parameters for compilation.
 */
data class SqliteCompilationParameters(
    /**
     * Toolchains.
     */
    val toolchains: Toolchains,

    /**
     * Version of SQLite.
     */
    val sqliteVersion: String,

    /**
     * Version of the SQLite MC library.
     */
    val sqliteMCVersion: String,

    /**
     * Minimum Android SDK.
     */
    val androidSdkMin: String,

    /**
     * Minimum macOS version.
     */
    val macosVersionMin: String,

    /**
     * Minimum iOS version.
     */
    val iosVersionMin: String,

    /**
     * Minimum tvOS version.
     */
    val tvosVersionMin: String,

    /**
     * Minimum watchOS version.
     */
    val watchosVersionMin: String,
) : Serializable {

    /**
     * Name of the SQLite product.
     * It is the name of the default C header file, C source file and code source function prefix.
     */
    val sqliteName: String = "sqlite${sqliteVersion.substringBefore('.')}"

    /**
     * Name of the SQLite product.
     * It is the name of the C header file, C source file and code source function prefix.
     */
    val sqliteMcName: String = "${sqliteName}mc_amalgamation"
}