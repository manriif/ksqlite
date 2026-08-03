package ksqlite.kapi.cipher

import ksqlite.types.cipher.SqliteMcCipherParams

/**
 * Represents a [DynamicCipher] configuration parameter.
 *
 * [Cipher configuration parameters](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#cipher-configuration-parameters)
 */
public typealias DynamicCipherParameter = SqliteMcCipherParams

/**
 * Allows to register dynamic cipher parameters.
 */
public interface DynamicCipherParameterRegistry {

    /**
     * Registers a new [DynamicCipherParameter] and [configure]s it.
     */
    public fun register(configure: DynamicCipherParameter.() -> Unit)
}