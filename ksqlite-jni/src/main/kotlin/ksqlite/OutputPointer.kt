package ksqlite

/**
 * Output pointer holding a value set on native side.
 */
public sealed class OutputPointer<Value>(initialValue: Value) {

    /**
     * Actual param value.
     */
    public var value: Value = initialValue

    /**
     * 32 bits signed integer output parameter.
     */
    public class OfInt32 @JvmOverloads constructor(initialValue: Int = 0) :
        OutputPointer<Int>(initialValue)

    /**
     * 64 bits signed integer output parameter.
     */
    public class OfInt64 @JvmOverloads constructor(initialValue: Long = 0L) :
        OutputPointer<Long>(initialValue)

    /**
     * 64 bits pointer output parameter.
     */
    public class OfPointer @JvmOverloads constructor(initialValue: Long = 0L) :
        OutputPointer<Long>(initialValue)

    /**
     * String output parameter.
     */
    public class OfString @JvmOverloads constructor(initialValue: String? = null) :
        OutputPointer<String?>(initialValue)

    /**
     * [Value] output parameter.
     */
    public class OfObject<Value> @JvmOverloads constructor(initialValue: Value? = null) :
        OutputPointer<Value?>(initialValue)
}