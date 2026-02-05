package tools

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

/**
 * Tool used for code/file generation or compilation.
 */
data class Tool(
    val version: String,
    val path: Provider<Directory>
)