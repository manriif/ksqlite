package ksqlite

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles

///////////////////////////////////////////////////////////////////////////
// Segment
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [MemorySegment] obtained from [block] provided non-null [value] or returns
 * [MemorySegment.NULL] if [value] is `null`.
 */
internal inline fun <T : Any> segment(value: T?, block: (T) -> MemorySegment): MemorySegment {
    return if (value == null) MemorySegment.NULL else block(value)
}

/**
 * Returns a [MemorySegment] to `this` [ByteArray]'s content.
 *
 * /!\ JVM GC moves memory so the content must be copied on native side and at call site or invalid
 * pointer may be accessed later.
 */
internal fun ByteArray?.pointer(): MemorySegment {
    return segment(this, MemorySegment::ofArray)
}

/**
 * Returns a [MemorySegment] representing a function that can be invoked on C-side.
 */
internal fun staticCFunction(
    arena: Arena,
    vararg argsLayout: ValueLayout,
    returnLayout: ValueLayout? = null,
    function: Function<*>
): MemorySegment {
    val functionDescriptor = returnLayout
        ?.let { FunctionDescriptor.of(it, *argsLayout) }
        ?: FunctionDescriptor.ofVoid(*argsLayout)

    val methodHandle = MethodHandles
        .lookup()
        .findVirtual(function::class.java, "invoke", functionDescriptor.toMethodType())
        .bindTo(function)

    return Linker
        .nativeLinker()
        .upcallStub(methodHandle, functionDescriptor, arena)
}

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [pointer] wrapping `this` [MemorySegment].
 */
internal fun wrap(segment: MemorySegment): pointer? = segment
    .takeUnless { it.address() == MemorySegment.NULL.address() }
    ?.let(::pointer)