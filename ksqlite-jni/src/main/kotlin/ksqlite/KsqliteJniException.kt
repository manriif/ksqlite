package ksqlite

/**
 * Exception that also define a [resultCode] that is returned from a sqlite callback invocation.
 * Methods of this class are expected to be invoked from JNI.
 */
@Suppress("unused")
public class KsqliteJniException(
    private val resultCode: Int,
    message: String
) : Exception(message) {

    /**
     * Returns the result code.
     */
    internal fun getResultCode(): Int {
        return resultCode
    }

    /**
     * Returns the message as UTF_8 encoded [ByteArray].
     */
    internal fun getMessageUtf8(): ByteArray? {
        return message?.toByteArray(Charsets.UTF_8)
    }
}