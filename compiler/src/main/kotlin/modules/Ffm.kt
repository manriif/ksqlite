package modules

import platform.Platform

/**
 * Returns the content of the FFM runtime metadata.
 */
fun createSqliteFfmRuntimeMetadataContent(
    packageName: String,
    nativeDirectoryName: String,
    libraryName: String,
    platforms: List<Platform>
): String = """
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