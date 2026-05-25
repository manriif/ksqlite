package ksqlite

/**
 * Base for function related callback.
 */
public interface FunctionCallback {

    /**
     * Function accepting a single sqlite3_context parameter.
     */
    public interface Func1 : FunctionCallback {

        /**
         * Invoked from JNI.
         */
        public fun call(context: Long)
    }

    /**
     * Function accepting a sqlite3_context parameter and an array of sqlite3_value.
     */
    public interface Func2 : FunctionCallback {

        /**
         * Invoked from JNI.
         */
        public fun call(
            context: Long,
            values: LongArray
        )
    }

    /**
     * xFunc function callback used in scalar function.
     */
    public fun interface Func : Func2

    /**
     * xStep function callback used in aggregate and window function.
     */
    public fun interface Step : Func2

    /**
     * xFinal function callback used in aggregate and window function.
     */
    public fun interface Final : Func1

    /**
     * xInverse function callback used in window function.
     */
    public fun interface Inverse : Func2

    /**
     * xValue function callback used in window function.
     */
    public fun interface Value : Func1
}