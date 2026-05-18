package ksqlite

/**
 * Exception that also define a [resultCode] that is returned from a sqlite callback invocation.
 * Methods of this class are expected to be invoked from JNI.
 */
@Suppress("unused")
public class KsqliteJniException(
    private val resultCode: Int,
    override val message: String
) : Exception(message) {

    /**
     * Returns the result code.
     */
    @JvmName("getResultCode")
    internal fun getResultCode(): Int {
        return resultCode
    }

    /**
     * Returns the message.
     */
    @JvmName("getMessage")
    internal fun getMessage(): String {
        return message
    }
}