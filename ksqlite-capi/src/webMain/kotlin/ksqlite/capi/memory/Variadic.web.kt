package ksqlite.capi.memory

import ksqlite.capi.memory.VariadicValue.OfPointer
import ksqlite.foreign.js.arrayForEachIndexed
import ksqlite.foreign.js.arraySize
import ksqlite.foreign.js.plus
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.sizeofIR
import kotlin.js.toJsBigInt

/**
 * Invokes a function accepting a variadic parameter.
 */
internal inline fun <Result> HeapAllocatorScope.invokeVariadic(
    values: Array<out VariadicValue<WasmPointer>?>,
    noinline manager: () -> MemoryManager,
    invoke: HeapAllocatorScope.(vaList: WasmPointer) -> Result
): Result {
    val pointerSize = memory.sizeofIR(IR.Ptr)
    val argCount = arraySize(values)
    val vaArgsSize = pointerSize * argCount
    val vaArgsPointer = allocate(vaArgsSize)

    arrayForEachIndexed(values) { index, value ->
        val vaArgPointer = vaArgsPointer + (index * pointerSize)

        when (value) {
            null -> memory.pokePtr(vaArgPointer, NullPtr)
            is OfInt -> memory.poke32(vaArgPointer, value.value)
            is OfUInt -> memory.poke32(vaArgPointer, value.value.toInt())
            is OfLong -> memory.poke64(vaArgPointer, value.value.toJsBigInt())
            is OfPointer -> memory.pokePtr(vaArgPointer, value.value)

            // String value is allocated on MemoryManager rather than current scope as the String is
            // tied to the lifecycle of the caller MemoryScope
            is OfString -> memory.pokePtr(
                address = vaArgPointer,
                value = manager().keyedStringPointer(
                    key = value.key,
                    value = value.value
                )
            )
        }
    }

    return invoke(vaArgsPointer)
}

/**
 * Invokes a function accepting a variadic parameter.
 */
internal inline fun <Result> invokeVariadic(
    values: Array<out VariadicValue<WasmPointer>?>,
    noinline manager: () -> MemoryManager,
    invoke: HeapAllocatorScope.(vaList: WasmPointer) -> Result
): Result = heapScoped {
    invokeVariadic(values, manager, invoke)
}

/**
 * Invokes a function accepting a variadic parameter.
 */
context(scope: HeapAllocatorScope)
internal inline fun <Result> invokeVariadic(
    noinline manager: () -> MemoryManager,
    vararg values: VariadicValue<WasmPointer>?,
    invoke: HeapAllocatorScope.(vaList: WasmPointer) -> Result
): Result = scope.invokeVariadic(values, manager, invoke)