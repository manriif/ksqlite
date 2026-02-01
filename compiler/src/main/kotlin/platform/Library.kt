package platform

/**
 * Library generation information.
 */
enum class Library(
    val sharedPrefix: String,
    val sharedSuffix: String,
    val staticPrefix: String,
    val staticSuffix: String
) {
    Darwin("lib", "dylib", "lib", "a"),
    Linux("lib", "so", "lib", "a"),
    MinGW("", "dll", "lib", "a");
}
