package ksqlite.kapi.cipher

/**
 * Scope to use with [DynamicCipher.Factory.create].
 */
public interface DynamicCipherCreateScope {

    /**
     * Returns the value for the parameter [name].
     *
     * @throws CipherException if [name] is not a parameter for the cipher.
     */
    public fun getParameter(name: String): Int
}