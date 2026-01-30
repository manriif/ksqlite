package toolchains

/**
 * Host information
 */
data class Host(
    val operatingSystem: OperatingSystem,
    val architecture: Architecture
) {

    /**
     * Operating Systems.
     */
    enum class OperatingSystem {
        Windows,
        MacOS,
        Linux
    }

    /**
     * CPU Architectures.
     */
    enum class Architecture {
        Arm64,
        X86_64,
    }

    companion object {

        /**
         * Current Host.
         */
        val Current = Host(
            operatingSystem = org.gradle.internal.os.OperatingSystem.current().run {
                when {
                    isMacOsX -> OperatingSystem.MacOS
                    isWindows -> OperatingSystem.Windows
                    isLinux -> OperatingSystem.Linux
                    else -> error("Unsupported operation system: $this")
                }
            },
            architecture = when (val architecture = System.getProperty("os.arch").lowercase()) {
                "aarch64", "arm64" -> Architecture.Arm64
                "x86_64", "amd64" -> Architecture.X86_64
                else -> error("Unsupported CPU architecture: $architecture")
            }
        )
    }
}