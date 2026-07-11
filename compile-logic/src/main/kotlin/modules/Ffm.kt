package modules

import komple.platform.Platform
import komple.project.c.CCompilation

private const val NATIVE_LIBS_RESOURCE_DIR_NAME = "native"
private const val OS_NAME = "osName"
private const val OS_ARCH = "osArch"

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
    packageName: String,
    libraryName: String,
    compilations: List<CCompilation>
): String = """
    |package $packageName
    |
    |/**
    | * Name of the Ksqlite native library.
    | */
    |internal const val KSQLITE_NATIVE_LIB_NAME: String = "$libraryName"
    |
    |/**
    | * Returns the path to the native library for [$OS_NAME] and [$OS_ARCH].
    | */
    |internal fun ksqliteLibPath($OS_NAME: String, $OS_ARCH: String) = when {
    |${
    compilations.joinToString("\n") { compilation ->
        val platform = compilation.platform.get()
        val libName = compilation.libraryFile.get().asFile.name
        val libPath = "${platform.ksqliteFfmResourceLibDirectory()}/$libName"

        val runtimeOsNameTest = when (platform.operatingSystem) {
            MacOS -> "isMacOs"
            Linux -> "isLinux"
            Windows -> "isWindows"
            else -> error("Non-desktop OSs aren't supported")
        }

        val runtimeOsArchTest = when (platform.architecture) {
            Arm64 -> "isArm64"
            X64 -> "isAmd64"
            else -> error("32-bit CPU architectures aren't supported")
        }

        """    $OS_NAME.$runtimeOsNameTest() && $OS_ARCH.$runtimeOsArchTest() -> "$libPath""""
    }
}
    |    else -> error("Unsupported platform: $$OS_NAME $$OS_ARCH")
    |}
""".trimMargin()