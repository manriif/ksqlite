@file:Suppress("DEPRECATION")

package ksqlite.capi.interop.wasm

import kotlin.js.JsAny
import kotlin.js.nativeInvoke

/**
 * JS invokable function.
 */
internal external interface JsFunction: JsAny {

    @nativeInvoke
    operator fun invoke(vararg args: JsAny): JsAny
}