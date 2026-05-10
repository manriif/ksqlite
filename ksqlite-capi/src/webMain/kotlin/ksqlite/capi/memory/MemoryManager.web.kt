package ksqlite.capi.memory

import ksqlite.capi.handlers.Handler
import ksqlite.capi.interop.Sqlite3Wasm
import ksqlite.capi.interop.js.toInt8Array
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.IR
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.interop.wasm.sizeofIR
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.wasm
import kotlin.js.toJsBigInt
import kotlin.js.toLong
import kotlin.reflect.KClass

internal actual class MemoryManager : MemoryManagerBase() {

    private val functionPointers by lazy { mutableMapOf<KClass<*>, WasmPointer>() }

    private val stableRefDisposer: WasmPointer by lazy {
        functionPointer(::StableRefDisposerHandler)
    }

    private val memory: Sqlite3Wasm
        inline get() = wasm

    ///////////////////////////////////////////////////////////////////////////
    // References
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the object, previously referenced using [stableRefPointer] and identified by
     * [pointer]'s address.
     *
     * @throws NullPointerException if there is no object associated with [pointer].
     */
    fun getStableRef(pointer: WasmPointer): Reference = notClosed {
        val refId = wasm.peek64(pointer).toLong().toULong()
        val reference = getDisposable<StableRefReference>(refId)
        return reference
    }

    /**
     * Returns a pointer to a function that disposes the reference previously obtained from
     * [stableRefPointer].
     *
     * Returns [NullPtr] if both [data] and [destructor] are `null`
     */
    fun stableRefDisposer(
        data: Any?,
        destructor: Sqlite3DestructorCallback? = null
    ): WasmPointer {
        return stableRefDisposer.takeIf { data != null || destructor != null } ?: NullPtr
    }

    override fun clear() {
        super.clear()
        functionPointers.clear()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Pointers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a [WasmPointer] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The resulting reference data can be accessed using [getStableRef] and it can be
     * disposed using [stableRefDisposer].
     */
    fun stableRefPointer(
        data: Any?,
        userData: sqlite3_mutable_pointer? = null,
        destructor: Sqlite3DestructorCallback? = null,
        key: String? = null
    ): WasmPointer = notClosed {
        if (data == null && destructor == null) {
            return NullPtr
        }

        val reference = registerDisposable(key) { id ->
            StableRefReference(id, destructor, data, userData)
        }

        return reference.pointer
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    private fun getOrCreateFunctionPointer(
        handlerKlass: KClass<out Handler>,
        factory: (MemoryManager) -> Handler
    ): WasmPointer = notClosed {
        functionPointers.getOrPut(handlerKlass) {
            val handler = factory(this)

            val reference = registerDisposable { id ->
                FunctionDisposable(id, handler)
            }

            reference.pointer
        }
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     *
     * Returns [NullPtr] if [callback] is `null`.
     */
    inline fun <reified H : Handler> functionPointer(
        callback: Any?,
        noinline factory: (MemoryManager) -> H
    ): WasmPointer = notClosed {
        if (callback == null) {
            return NullPtr
        }

        return getOrCreateFunctionPointer(H::class, factory)
    }

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    inline fun <reified H : Handler> functionPointer(
        noinline factory: (MemoryManager) -> H
    ): WasmPointer {
        return functionPointer(this, factory)
    }

    /**
     * Allocates a copy of the [value] and returns a [WasmPointer] to the content.
     *
     * This should preferably be used if there is no option to copy [value]'s content on native
     * side.
     *
     * Returns [NullPtr] if [value] is `null`.
     */
    fun byteArrayPointer(
        value: ByteArray?,
        destructor: Sqlite3DestructorCallback? = null
    ): WasmPointer = notClosed {
        if (value == null) {
            return NullPtr
        }

        val disposable = registerDisposable { id ->
            ByteArrayDisposable(id, value, destructor)
        }

        registerGlobalDisposable(disposable.pointer, disposable)
        return disposable.pointer
    }

    ///////////////////////////////////////////////////////////////////////////
    // Disposables
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Handler that dispose reference to object to make it available for GC.
     */
    private class StableRefDisposerHandler(manager: MemoryManager) : Handler(manager) {

        override fun WasmFunctions.install(): WasmPointer = installFunction(
            signature = FunctionSignature.Void(FunctionSignature.Pointer),
            function = v@{ refPointer: WasmPointer ->
                manager.getStableRef(refPointer).dispose()
            }
        )
    }

    /**
     * Disposable allocating a [WasmPointer].
     */
    private abstract inner class PointerDisposable(
        id: ULong,
        destructor: Sqlite3DestructorCallback?,
    ) : AutoDisposable(id, destructor) {

        abstract val pointer: WasmPointer

        override fun release() {
            memory.dealloc(pointer)
        }
    }

    /**
     * Reference to [data].
     */
    private inner class StableRefReference(
        id: ULong,
        destructor: Sqlite3DestructorCallback?,
        override val data: Any?,
        override val userData: sqlite3_mutable_pointer?,
    ) : PointerDisposable(id, destructor),
        Reference {

        override val pointer = memory.run {
            alloc(sizeofIR(IR.I64)).also { address ->
                poke64(address, id.toLong().toJsBigInt())
            }
        }
    }

    /**
     * Reference to a function.
     */
    private inner class FunctionDisposable(
        id: ULong,
        handler: Handler,
        destructor: Sqlite3DestructorCallback? = null
    ) : PointerDisposable(id, destructor) {

        override val pointer = with(handler) {
            memory.install()
        }

        override val userData: sqlite3_mutable_pointer?
            get() = null
    }

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: ULong,
        byteArray: ByteArray,
        destructor: Sqlite3DestructorCallback?,
    ) : PointerDisposable(id, destructor) {

        private val typedArray = toInt8Array(byteArray)
        override val pointer = memory.allocFromTypedArray(typedArray)

        override val userData: sqlite3_mutable_pointer? by lazy {
            sqlite3_mutable_pointer.from(pointer, typedArray.byteLength.toLong(), memory)
        }

        override fun release() {
            super.release()
            unregisterGlobalDisposable(pointer)
        }
    }
}