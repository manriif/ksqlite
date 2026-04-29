package modules

import komple.platform.Platform
import komple.project.c.CProject

/**
 * Returns the content of the FFM runtime metadata.
 */
fun createSqliteFfmRuntimeMetadataContent(
    cProject: CProject,
    packageName: String,
    nativeDirectoryName: String,
    platforms: List<Platform>
): String {
    val libraryName = cProject.libraryName

    return """
        |package $packageName
        |
        |/**
        | * Name of the Ksqlite native library.
        | */
        |public const val KSQLITE_NATIVE_LIB_NAME: String = "$libraryName"
        |
        |${
            platforms.joinToString("\n\n") { platform ->
                val libName = platform.operatingSystem.library.run {
                    "$sharedPrefix$libraryName.$sharedSuffix"
                }
    
                val uppercaseLibName = platform.name.uppercase()
                val libPath = "$nativeDirectoryName/${platform.name}/$libName"
    
                """
                |/**
                | * Path to the Ksqlite library for the `${platform.operatingSystem.name}` operating
                | * system and `${platform.architecture.name}` architecture.
                | */
                |internal const val KSQLITE_NATIVE_LIB_${uppercaseLibName}_PATH: String = "$libPath"
            """.trimMargin()
            }
        }
    """.trimMargin()
}