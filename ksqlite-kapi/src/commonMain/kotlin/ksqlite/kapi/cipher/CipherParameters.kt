package ksqlite.kapi.cipher

import ksqlite.types.cipher.SqliteMcCipher
import ksqlite.types.cipher.SqliteMcConfigCipherParam
import ksqlite.types.cipher.SqliteMcConfigParamPrefix

/**
 * Reads and writes the parameter values of a given [Cipher].
 */
public interface CipherParameters<Cipher : SqliteMcCipher> {

    /**
     * Returns the [Value] for the given cipher [param].
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred while reading the value.
     */
    public fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> get(
        param: Param,
        prefix: SqliteMcConfigParamPrefix
    ): Value

    /**
     * Returns the transient [Value] for the given cipher [param].
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred while reading the value.
     */
    public operator fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> get(
        param: Param
    ): Value = get(param, None)

    /**
     * Sets the [value] for the given cipher [param] and returns the current parameter value.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred while writing the value.
     */
    public fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> set(
        param: Param,
        value: Value,
        prefix: SqliteMcConfigParamPrefix.ReadWrite
    )

    /**
     * Sets the permanent [value] for the given cipher [param].
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurred while writing the value.
     */
    public operator fun <Value : Any, Param : SqliteMcConfigCipherParam<Cipher, Value>> set(
        param: Param,
        value: Value
    ): Unit = set(param, value, Default)
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the value for the given dynamic cipher [param].
 * The transient value is returned by default.
 *
 * @throws ksqlite.kapi.SQLiteException if an error occurred while writing the value.
 */
public fun CipherParameters<SqliteMcCipher.Dynamic>.get(
    param: String,
    prefix: SqliteMcConfigParamPrefix.ReadWrite = SqliteMcConfigParamPrefix.None
): Int = get(
    param = SqliteMcCipher.Dynamic.Parameter(param),
    prefix = prefix
)

/**
 * Sets the [value] for the given dynamic cipher [param].
 * The value is written permanently by default.
 *
 * @throws ksqlite.kapi.SQLiteException if an error occurred while writing the value.
 */
public fun CipherParameters<SqliteMcCipher.Dynamic>.set(
    param: String,
    value: Int,
    prefix: SqliteMcConfigParamPrefix.ReadWrite = SqliteMcConfigParamPrefix.Default
): Unit = set(
    param = SqliteMcCipher.Dynamic.Parameter(param),
    value = value,
    prefix = prefix
)