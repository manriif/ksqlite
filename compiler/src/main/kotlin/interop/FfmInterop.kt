package interop

import platform.Platform

/**
 * Returns the content of the FFM runtime metadata.
 */
fun createSqliteFfmRuntimeMetadataContent(
    packageName: String,
    nativeDirectoryName: String,
    platforms: List<Platform>
): String = """
    |package $packageName
    |
    |${
    platforms.joinToString("\n\n") { platform ->
        """public const val KSQLITE_NATIVE_LIB_${platform.name.uppercase()}_PATH: String = "$nativeDirectoryName/${platform.name}""""
    }
}
""".trimMargin()