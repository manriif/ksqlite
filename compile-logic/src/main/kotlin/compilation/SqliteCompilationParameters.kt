package compilation

import tools.Toolchains
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
     * Name of the generated library.
     */
    val libraryName: String,

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
     * It is the name of the SQLite C header file, C source file and code source function prefix.
     */
    val sqliteName: String = "sqlite${sqliteVersion.substringBefore('.')}"

    /**
     * Name of the SQLite Multiple Ciphers.
     * It is the name of the code source function prefix.
     */
    val sqliteMcName: String = "${sqliteName}mc"

    /**
     * Name of the SQLite Multiple Ciphers amalgamation files.
     * It is the name of the C header file, C source file.
     */
    val sqliteMcAmalgamationName: String = "${sqliteMcName}_amalgamation"
}