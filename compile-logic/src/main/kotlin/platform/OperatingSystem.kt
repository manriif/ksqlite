@file:Suppress("unused")

package platform

import java.io.Serializable

/**
 * Operating Systems.
 */
sealed interface OperatingSystem : Serializable {

    /**
     * Name of the operating system.
     */
    val name: String

    /**
     * Library information.
     */
    val library: Library

    /**
     * Supported by toolchains for compilation.
     */
    sealed interface Host : OperatingSystem

    ///////////////////////////////////////////////////////////////////////////
    // Darwin
    ///////////////////////////////////////////////////////////////////////////

    sealed class Darwin : OperatingSystem {

        final override val library: Library
            get() = Library.Darwin
    }

    data object MacOS : Darwin(), Host {

        override val name: String
            get() = "macos"

        private fun readResolve(): Any = MacOS
    }

    sealed class IOS : Darwin() {

        override val name: String
            get() = "ios"

        data object Device : IOS() {
            private fun readResolve(): Any = Device
        }

        data object Simulator : IOS() {
            private fun readResolve(): Any = Simulator
        }
    }

    sealed class TvOS : Darwin() {

        override val name: String
            get() = "tvos"

        data object Device : TvOS() {
            private fun readResolve(): Any = Device
        }

        data object Simulator : TvOS() {
            private fun readResolve(): Any = Simulator
        }
    }

    sealed class WatchOS : Darwin() {

        override val name: String
            get() = "watchos"

        data object Device : WatchOS() {
            private fun readResolve(): Any = Device
        }

        data object DeviceGen2 : WatchOS() {
            private fun readResolve(): Any = DeviceGen2
        }

        data object Simulator : WatchOS() {
            private fun readResolve(): Any = Simulator
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Linux
    ///////////////////////////////////////////////////////////////////////////

    sealed class LinuxLike: OperatingSystem {

        final override val library: Library
            get() = Library.Linux
    }

    data object Android : LinuxLike() {

        override val name: String
            get() = "android"

        private fun readResolve(): Any = Android
    }

    data object Linux : LinuxLike(), Host {

        override val name: String
            get() = "linux"

        private fun readResolve(): Any = Linux
    }

    ///////////////////////////////////////////////////////////////////////////
    // Windows
    ///////////////////////////////////////////////////////////////////////////

    data object Windows : Host {

        override val name: String
            get() = "windows"

        override val library: Library
            get() = Library.MinGW

        private fun readResolve(): Any = Windows
    }
}