package platform

/**
 * Host information
 */
data class Host(
    val operatingSystem: OperatingSystem.Host,
    val architecture: Architecture.Host
) {

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
                "x86_64", "amd64" -> Architecture.X64
                else -> error("Unsupported CPU architecture: $architecture")
            }
        )
    }
}