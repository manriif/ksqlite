package modules

import komple.platform.Platform
import komple.project.c.CCompilation
import komple.project.c.CProject

private const val NATIVE_LIBS_RESOURCE_DIR_NAME = "native"

/**
 * Returns the path to the directory where the generated shared library for `this` [Platform] should
 * be placed into relatively to the jar resources root.
 */
fun Platform.ksqliteFfmResourceLibDirectory(): String {
    return "$NATIVE_LIBS_RESOURCE_DIR_NAME/$name"
}

/**
 * Returns the content of the FFM runtime metadata.
 */
fun createKsqliteFfmRuntimeMetadataContent(
    cProject: CProject,
    compilations: List<CCompilation>
): String = """
    |package ${cProject.packageName.get()}
    |
    |/**
    | * Name of the Ksqlite native library.
    | */
    |public const val KSQLITE_NATIVE_LIB_NAME: String = "${cProject.libraryName.get()}"
    |
    |${
    compilations.joinToString("\n\n") { compilation ->
        val libName = compilation.libraryFile.get().asFile.name
        val libPath = "${compilation.platform.ksqliteFfmResourceLibDirectory()}/$libName"
        val pathKey = "KSQLITE_NATIVE_LIB_${compilation.platform.name.uppercase()}_PATH"
        val osName = compilation.platform.operatingSystem.name
        val archName = compilation.platform.architecture.name

        """
            |/**
            | * Path to the Ksqlite library for the `$osName` operating system and 
            | * `$archName` architecture.
            | */
            |internal const val $pathKey: String = "$libPath"
        """.trimMargin()
    }
}
""".trimMargin()