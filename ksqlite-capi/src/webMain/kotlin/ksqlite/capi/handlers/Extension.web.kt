package ksqlite.capi.handlers

import ksqlite.capi.autoExtensionHandle
import ksqlite.capi.exports
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.StaticMemoryManager
import ksqlite.capi.memory.isNull
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines
import kotlin.js.toJsString
import kotlin.js.toLong

/**
 * Singleton handler for auto extensions.
 */
internal val SharedAutoExtensionHandler by lazy {
    StaticMemoryManager.functionPointer(::AutoExtensionHandler)
}

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 */
internal class AutoExtensionHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = ::handle
    )

    private fun handle(
        db: WasmPointer,
        pzErrMsg: WasmPointer,
        pApi: WasmPointer
    ): Int = autoExtensionHandle(
        db = sqlite3(db),
        api = sqlite3_api_routines(pApi),
        errorPointer = pzErrMsg.takeUnless(WasmPointer::isNull)
    ) { errorPointer, message ->
        val bytes = memory.jstrToUintArray(message.toJsString(), true)
        val destinationPointer = exports.sqlite3_malloc(bytes.length)

        if (!destinationPointer.isNull) {
            memory.heap8u().set(bytes, destinationPointer.toLong().toInt())
            memory.pokePtr(errorPointer, destinationPointer)
        }
    }
}