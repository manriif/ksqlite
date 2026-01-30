package toolchains

import java.io.Serializable

/**
 * Available toolchains.
 */
data class Toolchains(
    /**
     * The Android NDK.
     */
    val android: Toolchain
) : Serializable