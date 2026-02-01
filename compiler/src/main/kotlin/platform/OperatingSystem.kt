@file:Suppress("unused")

package platform

import java.io.Serializable

/**
 * Operating Systems.
 */
sealed interface OperatingSystem : Serializable {

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
        private fun readResolve(): Any = MacOS
    }

    sealed class IOS : Darwin() {

        data object Device : IOS() {
            private fun readResolve(): Any = Device
        }

        data object Simulator : IOS() {
            private fun readResolve(): Any = Simulator
        }
    }

    sealed class TvOS : Darwin() {

        data object Device : TvOS() {
            private fun readResolve(): Any = Device
        }

        data object Simulator : TvOS() {
            private fun readResolve(): Any = Simulator
        }
    }

    sealed class WatchOS : Darwin() {

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
        private fun readResolve(): Any = Android
    }

    data object Linux : LinuxLike(), Host {
        private fun readResolve(): Any = Linux
    }

    ///////////////////////////////////////////////////////////////////////////
    // Windows
    ///////////////////////////////////////////////////////////////////////////

    data object Windows : Host {

        override val library: Library
            get() = Library.MinGW

        private fun readResolve(): Any = Windows
    }
}