package ksqlite.capi.handlers

import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.stableRefDataHolder
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles

/**
 * Handler for upcall stub.
 */
internal abstract class Handler {

    lateinit var manager: MemoryManager

    /**
     * Allocates a new upcall stub.
     */
    abstract fun allocate(arena: Arena): MemorySegment

    /**
     * Returns [block]'s result, invoked with [Data] and optional userData obtained from a
     * previously referenced [refPointer].
     */
    protected inline fun <reified Data : Any, Result> handle(
        refPointer: MemorySegment,
        block: (data: Data, appData: Any?) -> Result
    ): Result = manager.stableRefDataHolder<Data, Any?>(refPointer).run {
        block(data, appData)
    }
}

/**
 * [Handler] that receive a single pointer to a reference.
 */
internal abstract class ReferenceHandler : Handler() {

    final override fun allocate(arena: Arena): MemorySegment {
        val functionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)

        val methodHandle = MethodHandles
            .lookup()
            .findVirtual(ReferenceHandler::class.java, "apply", functionDescriptor.toMethodType())
            .bindTo(this)

        return Linker
            .nativeLinker()
            .upcallStub(methodHandle, functionDescriptor, arena)
    }

    protected abstract fun apply(refPointer: MemorySegment)
}