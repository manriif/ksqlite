package sqlite

import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsModule
import kotlin.js.Promise
import kotlin.js.definedExternally
import kotlin.js.toJsArray

///////////////////////////////////////////////////////////////////////////
// Config
///////////////////////////////////////////////////////////////////////////

/**
 * Configuration only existing in the Ksqlite patched WASM trunk.
 */
internal external interface SqliteModuleConfig : JsAny {

    /**
     * Optional callback invoked for logging messages.
     */
    var customDebugModule: ((JsAny?, JsAny?, JsAny?, JsAny?, JsAny?, JsAny?, JsAny?) -> Unit)?

    /**
     * Optional callback used to resolve wasm file location.
     */
    var customLocateFile: ((String, String) -> JsAny?)
}

/**
 * Returns a [SqliteModuleConfig] instance.
 */
@JsFun("() => ({})")
private external fun createSqliteModuleConfig(): SqliteModuleConfig

///////////////////////////////////////////////////////////////////////////
// Module
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes the sqliteInitModule default exported function returning a promise resolving the SQLite
 * instance.
 */
@JsModule("./sqlite3-64bit.mjs")
private external fun sqliteInitModule(config: SqliteModuleConfig = definedExternally): Promise<JsAny>

/**
 * Returns a [Promise] resolving an SQLite instance.
 */
public fun sqliteInitializer(
    debugModule: ((args: JsArray<out JsAny>) -> Unit)?,
    locateFile: ((path: String, prefix: String) -> JsAny?)?
): Promise<JsAny> {
    val config = createSqliteModuleConfig().apply {
        debugModule?.let { debug ->
            customDebugModule = { arg1, arg2, arg3, arg4, arg5, arg6, arg7 ->
                debug(listOfNotNull(arg1, arg2, arg3, arg4, arg5, arg6, arg7).toJsArray())
            }
        }

        locateFile?.let { locate ->
            customLocateFile = locate
        }
    }

    return sqliteInitModule(config)
}