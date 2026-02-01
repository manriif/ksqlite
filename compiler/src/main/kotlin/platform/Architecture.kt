@file:Suppress("unused")

package platform

import java.io.Serializable

/**
 * CPU Architectures.
 */
sealed interface Architecture : Serializable {

    /**
     * Supported by toolchains for compilation.
     */
    sealed interface Host : Architecture

    ///////////////////////////////////////////////////////////////////////////
    // ARM
    ///////////////////////////////////////////////////////////////////////////

    data object Arm64 : Host {
        private fun readResolve(): Any = Arm64
    }

    data object Arm32 : Architecture {
        private fun readResolve(): Any = Arm32
    }

    ///////////////////////////////////////////////////////////////////////////
    // x86
    ///////////////////////////////////////////////////////////////////////////

    data object X64 : Host {
        private fun readResolve(): Any = X64
    }

    data object X86 : Architecture {
        private fun readResolve(): Any = X86
    }
}