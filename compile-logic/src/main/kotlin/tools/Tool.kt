package tools

import java.io.Serializable

/**
 * Tool used for code/file generation or compilation.
 */
data class Tool(
    /**
     * Version of the toolchain.
     */
    val version: String,

    /**
     * Absolute path to the toolchain.
     */
    val path: String
) : Serializable