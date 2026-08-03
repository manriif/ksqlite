package ksqlite.kapi.cipher

/**
 * Exception thrown when an error related to a cipher API happens.
 */
public class CipherException(
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause)

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Throws a [CipherException] with a message indicating the parameter read failure.
 */
internal fun throwCipherException(message: String): Nothing {
    throw CipherException(message)
}

/**
 * Throws a [CipherException] with a message indicating the parameter read failure.
 */
internal fun throwParameterReadFailedCipherException(): Nothing {
    throwCipherException("Failed to read the parameter value")
}

/**
 * Throws a [CipherException] with a message indicating the parameter write failure.
 */
internal fun throwParameterWriteFailedCipherException(): Nothing {
    throwCipherException("Failed to write the parameter value")
}