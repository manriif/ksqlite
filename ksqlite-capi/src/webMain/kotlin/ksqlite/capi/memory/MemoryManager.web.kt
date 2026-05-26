@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.handlers.Handler
import ksqlite.capi.interop.Sqlite3Wasm
import ksqlite.capi.interop.js.toInt8Array
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.IR
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.allocCString
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.interop.wasm.sizeofIR
import ksqlite.capi.wasm
import kotlin.js.toJsBigInt
import kotlin.js.toLong
import kotlin.reflect.KClass

internal actual class MemoryManager : MemoryManagerBase() {

    private val functionPointers: MutableMap<KClass<*>, WasmPointer> by lazy(::mutableMapOf)
    private val stableRefDisposer by lazy { functionPointer(::StableRefDisposerHandler) }

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
    fun <AppData> getStableRef(pointer: WasmPointer): Reference<AppData> = notClosed {
        val refId = wasm.peek64(pointer).toLong()
        val reference = getDisposable<AppData, StableRefReference<AppData>>(refId)
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
        destructor: Sqlite3DestroyCallback<*>? = null
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
     * The returned reference data can later be accessed using [getStableRef] and it can be disposed
     * using [stableRefDisposer].
     */
    private fun <AppData> commonStableRefPointer(
        key: String?,
        data: Any?,
        appData: AppData,
        destructor: Sqlite3DestroyCallback<AppData>?
    ): WasmPointer = notClosed {
        if (data == null && destructor == null) {
            NullPtr
        } else {
            registerDisposable(key) { StableRefReference(it, destructor, data, appData) }.pointer
        }
    }

    /**
     * Returns a [WasmPointer] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can be accessed using [getStableRef] and it can be
     * disposed using [stableRefDisposer].
     */
    fun <AppData> stableRefPointer(
        data: Any?,
        appData: AppData,
        destructor: Sqlite3DestroyCallback<AppData>? = null,
        key: String? = null
    ): WasmPointer = commonStableRefPointer(null, data, appData, destructor)

    /**
     * Returns a [WasmPointer] referring [data] or `null` if both [data] and [destructor] are
     * `null`.
     *
     * The returned reference data can be accessed using [getStableRef] and it can be
     * disposed using [stableRefDisposer].
     *
     * If a pointer was previously obtained using [key], it is disposed.
     */
    fun <AppData> keyedStableRefPointer(
        key: String,
        data: Any?,
        appData: AppData,
        destructor: Sqlite3DestroyCallback<AppData>? = null
    ): WasmPointer = commonStableRefPointer(key, data, appData, destructor)

    /**
     * Returns a pointer to a static function that will invoke the `handle` function of the
     * [Handler] returned by [factory].
     */
    private fun getOrCreateFunctionPointer(
        handlerKlass: KClass<out Handler>,
        factory: (MemoryManager) -> Handler
    ): WasmPointer = notClosed {
        functionPointers.getOrPut(handlerKlass) {
            val handler = factory(this).apply {
                this.memory = this@MemoryManager.memory
            }

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
     * The resulting disposable can be disposed using [globalDisposer].
     */
    fun byteArrayPointer(
        value: ByteArray,
        destructor: Sqlite3DestroyCallback<ByteArray>?
    ): WasmPointer = notClosed {
        registerDisposable { ByteArrayDisposable(it, destructor, value) }.pointer
    }

    /**
     * Allocates a C-string with [value]'s content and returns a [WasmPointer] to the content.
     */
    fun keyedStringPointer(
        key: String,
        value: String
    ): WasmPointer = notClosed {
        registerDisposable(key) { StringDisposable(it, value) }.pointer
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
            function = { refPointer: WasmPointer ->
                manager.getStableRef<Nothing?>(refPointer).dispose()
            }
        )
    }

    /**
     * Disposable allocating a [WasmPointer].
     */
    private abstract inner class PointerDisposable<AppData>(
        id: Long,
        destructor: Sqlite3DestroyCallback<AppData>?,
    ) : AutoDisposable<AppData>(id, destructor) {

        abstract val pointer: WasmPointer

        override fun release() {
            memory.dealloc(pointer)
        }
    }

    /**
     * Reference to [data].
     */
    private inner class StableRefReference<AppData>(
        id: Long,
        destructor: Sqlite3DestroyCallback<AppData>?,
        override val data: Any?,
        override val appData: AppData
    ) : PointerDisposable<AppData>(id, destructor),
        Reference<AppData> {

        override val pointer = memory.run {
            alloc(sizeofIR(IR.I64)).also { address ->
                poke64(address, id.toJsBigInt())
            }
        }
    }

    /**
     * Reference to [Handler].
     */
    private inner class FunctionDisposable(
        id: Long,
        override val appData: Handler
    ) : PointerDisposable<Handler>(id, null) {

        override val pointer = with(appData) {
            this@MemoryManager.memory.install()
        }
    }

    /**
     * Reference to [ByteArray].
     */
    private inner class ByteArrayDisposable(
        id: Long,
        destructor: Sqlite3DestroyCallback<ByteArray>?,
        override val appData: ByteArray,
    ) : PointerDisposable<ByteArray>(id, destructor) {

        override val pointer = memory.allocFromTypedArray(toInt8Array(appData))

        init {
            registerGlobalDisposable(pointer, this)
        }

        override fun release() {
            super.release()
            unregisterGlobalDisposable(pointer)
        }
    }

    /**
     * Reference to [String].
     */
    private inner class StringDisposable(
        id: Long,
        override val appData: String
    ) : PointerDisposable<String>(id, null) {

        override val pointer = memory.allocCString(appData)
    }
}