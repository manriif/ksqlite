@file:OptIn(ExperimentalWasmJsInterop::class)

package ksqlite.capi

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.toJsString

@JsFun("""(args) => console.log(...args)""")
private external fun log(vararg args: JsAny?)

internal actual suspend fun loadSqliteForTest(): Boolean {
    if (!isSqliteLoaded) {
        sqliteLoad {
            debugModule = ::log

            fileLocator = { path, _ ->
                "base/kotlin/$path".toJsString()
            }
        }
    }

    return true
}

/**
 * FIXME: For now, `/tmp` seems to exist in the default 'unix' VFS so stick with it for testing
 */
internal actual fun temporaryTestDirectory(subdirectory: String): String = "/tmp"