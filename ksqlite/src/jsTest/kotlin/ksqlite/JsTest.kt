package ksqlite

import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

@JsModule("./sqlite3-64bit.mjs")
external fun initSqlite3(config: dynamic = definedExternally): Promise<dynamic>

private fun debug(
    arg1: Any?,
    arg2: Any? = null,
    arg3: Any? = null,
    arg4: Any? = null,
    arg5: Any? = null,
    arg6: Any? = null,
    arg7: Any? = null,
) {
    val args = listOfNotNull(arg1, arg2, arg3, arg4, arg5, arg6, arg7).toTypedArray()
    console.log("SQLITE: ", *args)
    console.log("\n")
}

class JsTest {

    @Test
    fun initJs() = runTest(timeout = 60.minutes) {
        val module = js("""{}""")

        with(module) {
            customDebugModule = ::debug
            //customLocateFile = { path: String, prefix: String -> wasmUrl }
        }

        val sqlite = runCatching { initSqlite3(module).await() }
        console.log(sqlite)
    }
}