/**
 * Checksums of downloadable content.
 */
data class KsqliteChecksums(
    val androidNdkLinux: String,
    val androidNdkMacos: String,
    val androidNdkWindows: String,
    val jextractLinuxAarch64: String,
    val jextractLinuxX64: String,
    val jextractMacosAarch64: String,
    val jextractMacosX64: String,
    val jextractWindowsX64: String,
    val sqlite: String,
    val sqliteMc: String,
    val emsdk: String
)