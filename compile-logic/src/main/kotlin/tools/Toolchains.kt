package tools

import java.io.Serializable

/**
 * Toolchains.
 */
data class Toolchains(
    val android: Tool
) : Serializable