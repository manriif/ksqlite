package ksqlite.capi

/**
 * Lists all the options defined at compile-time.
 */
@Suppress("FoldInitializerAndIfToElvis")
public fun sqliteCompileOptions(): List<String> {
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