package ksqlite.kapi

import ksqlite.capi.sqlite3_compileoption_get
import ksqlite.capi.sqlite3_complete
import ksqlite.capi.sqlite3_hard_heap_limit64
import ksqlite.capi.sqlite3_keyword_check
import ksqlite.capi.sqlite3_keyword_count
import ksqlite.capi.sqlite3_keyword_name
import ksqlite.capi.sqlite3_libversion
import ksqlite.capi.sqlite3_libversion_number
import ksqlite.capi.sqlite3_log
import ksqlite.capi.sqlite3_soft_heap_limit64
import ksqlite.capi.sqlite3_sourceid
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.sqliteResultThrow
import ksqlite.kapi.helpers.usingParam
import ksqlite.types.SqliteCompleteResult

internal object SQLiteStaticImpl : SQLiteStatic {

    override val compileOptions by lazy(::sqliteListCompileOptions)

    override val keywordCount: Int
        get() = sqlite3_keyword_count()

    override val version: String
        get() = sqlite3_libversion()

    override val versionNumber: Int
        get() = sqlite3_libversion_number()

    override val sourceId: String
        get() = sqlite3_sourceid()

    override fun isCompleteSqlStatement(sql: String): Boolean {
        return when (val result = sqlite3_complete(sql)) {
            SqliteCompleteResult.Complete -> true
            SqliteCompleteResult.Incomplete -> false
            is SqliteCompleteResult.Failure -> sqliteResultThrow(result.result, null)
        }
    }

    override fun isKeyword(word: String): Boolean = sqlite3_keyword_check(word) != 0

    override fun getKeyword(index: Int): String = usingParam(Utf8OutputParam()) { outName ->
        sqliteResultCheck(sqlite3_keyword_name(index, outName))
    }

    override fun log(errorCode: Int, message: String) = sqlite3_log(errorCode, message)
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Lists all the options defined at compile-time.
 */
@Suppress("FoldInitializerAndIfToElvis")
private fun sqliteListCompileOptions(): List<String> {
    var option: String? = sqlite3_compileoption_get(0)

    if (option == null) {
        return emptyList()
    }

    val options = mutableListOf(option)
    var index = 1

    do {
        option = sqlite3_compileoption_get(index++)
        option?.let(options::add)
    } while (option != null)

    return options.toList()
}