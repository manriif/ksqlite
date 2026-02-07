package ksqlite

import kotlin.js.Promise

@JsModule("./esm64/sqlite3-64bit.mjs")
internal external object Sqlite3Module {
    fun default(config: dynamic = definedExternally): Promise<dynamic>
}

private val wasmUrl = js("""new URL('./esm64/sqlite3-64bit.wasm', import.meta.url).href""").unsafeCast<String>()

private fun debug(arg: Any) {
    console.log("DEBUG: ", arg)
    console.log("\n")
}

@OptIn(ExperimentalJsExport::class)
@JsExport
internal fun configureSqlite(sIMS: dynamic) {
    sIMS.sqlite3Dir = "kotlin/"
    sIMS.wasmFilename = "sqlite3-64bit.wasm"
    sIMS.debugModule = ::debug

    sIMS.locateFile = { path: String, _: String ->
        (if (path == sIMS.wasmFilename) {
            wasmUrl
        } else {
            path
        }).also {
            debug("url = $it")
        }
    }
}

public actual val sqliteLibVersion: String
    get() = "cfgvhg"