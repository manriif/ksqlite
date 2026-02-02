import ksqlite.sqliteLibVersion
import kotlin.test.Test
import kotlin.test.assertTrue

class KsqliteTest {

    @Test
    fun `version is returned`() {
        assertTrue { sqliteLibVersion.isNotBlank() }
    }
}