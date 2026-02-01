package platform

import java.io.Serializable

/**
 * Platform information.
 */
data class Platform(
    val operatingSystem: OperatingSystem,
    val architecture: Architecture,
) : Serializable {

    /**
     * Name of the platform.
     */
    val name: String
        get() = "${operatingSystem.name}_${architecture.name}"
}