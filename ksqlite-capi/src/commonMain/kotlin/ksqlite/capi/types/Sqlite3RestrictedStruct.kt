package ksqlite.capi.types

/**
 * Holder for a [Pointer] to an sqlite3_* struct which has some limitations due to how memory is
 * managed.
 *
 * The [pointer] is not managed from the Kotlin side. To prevent memory leaks, some sqlite functions
 * calls which accept the [pointer] as an argument will throw an [UnsupportedOperationException].
 *
 * These prohibited functions are those accepting non-primitive arguments and storing them for later
 * use after they return. Are included:
 *
 * - Functions accepting a callback
 * - Functions accepting a String which is expected to be static
 * - Functions accepting a ByteArray and for which sqlite do not offer copy possibility
 *
 * Functions that are not listed above should work as usual.
 */
public interface Sqlite3RestrictedStruct<Pointer> {

    /**
     * Pointer to the restricted sqlite3 struct.
     */
    public val pointer: Pointer
}

/**
 * [Sqlite3Param] which instantiate [Value] lazily using [factory].
 */
internal class LazySqlite3RestrictedStruct<Value>(factory: () -> Value) : Sqlite3RestrictedStruct<Value> {

    override val pointer: Value by lazy(factory)
}

/**
 * Returns a [LazySqlite3RestrictedStruct] instance.
 */
internal fun <Value> restricted(factory: () -> Value): LazySqlite3RestrictedStruct<Value> {
    return LazySqlite3RestrictedStruct(factory)
}
