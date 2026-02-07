package ksqlite

import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class JsTest {

    @Test
    fun initJs() = runTest(timeout = 60.minutes) {
        js("""globalThis""").kotlinSqliteConfigure = ::configureSqlite
        val module = runCatching { Sqlite3Module.default().await() }
        console.log('\n', module)
    }
}