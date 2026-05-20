package ksqlite

/**
 * Output pointer holding a value set on native side.
 */
public sealed class OutputPointer<Value>(initialValue: Value) {

    /**
     * Actual param value.
     */
    public var value: Value = initialValue
        internal set

    /**
     * 32 bits signed integer output parameter.
     */
    public class OfInt32(initialValue: Int = 0) : OutputPointer<Int>(initialValue)

    /**
     * 64 bits signed integer output parameter.
     */
    public class OfInt64(initialValue: Long = 0L) : OutputPointer<Long>(initialValue)

    /**
     * 64 bits pointer output parameter.
     */
    public class OfPointer(initialValue: Long): OutputPointer<Long>(initialValue)

    /**
     * String output parameter.
     */
    public class OfString(initialValue: String? = null): OutputPointer<String?>(initialValue)
}