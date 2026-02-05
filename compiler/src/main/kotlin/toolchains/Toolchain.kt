package toolchains

import java.io.Serializable

/**
 * Toolchain information.
 */
data class Toolchain(
    /**
     * Version of the toolchain.
     */
    val version: String,

    /**
     * Absolute path to the toolchain.
     */
    val path: String
) : Serializable