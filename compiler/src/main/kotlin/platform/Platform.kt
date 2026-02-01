package platform

import java.io.Serializable

/**
 * Platform information.
 */
data class Platform(
    val operatingSystem: OperatingSystem,
    val architecture: Architecture,
) : Serializable