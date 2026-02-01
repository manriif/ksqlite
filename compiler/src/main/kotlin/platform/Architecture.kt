@file:Suppress("unused")

package platform

import java.io.Serializable

/**
 * CPU Architectures.
 */
sealed interface Architecture : Serializable {

    /**
     * Name of the architecture.
     */
    val name: String

    /**
     * Supported by toolchains for compilation.
     */
    sealed interface Host : Architecture

    ///////////////////////////////////////////////////////////////////////////
    // ARM
    ///////////////////////////////////////////////////////////////////////////

    data object Arm64 : Host {

        override val name: String
            get() = "aarch64"

        private fun readResolve(): Any = Arm64
    }

    data object Arm32 : Architecture {

        override val name: String
            get() = "aarch32"

        private fun readResolve(): Any = Arm32
    }

    ///////////////////////////////////////////////////////////////////////////
    // x86
    ///////////////////////////////////////////////////////////////////////////

    data object X64 : Host {

        override val name: String
            get() = "x86_64"

        private fun readResolve(): Any = X64
    }

    data object X86 : Architecture {

        override val name: String
            get() = "x86"

        private fun readResolve(): Any = X86
    }
}