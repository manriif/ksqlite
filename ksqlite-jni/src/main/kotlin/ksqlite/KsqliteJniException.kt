package ksqlite

/**
 * Exception that also define a [resultCode] that is returned from a sqlite callback invocation.
 */
public class KsqliteJniException(
    private val resultCode: Int,
    message: String
) : Exception(message) {

    internal fun getResultCode(): Int {
        return resultCode
    }
}