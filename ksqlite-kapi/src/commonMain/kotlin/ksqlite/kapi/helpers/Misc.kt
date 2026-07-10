package ksqlite.kapi.helpers

/**
 * Lists elements obtained from [get], incrementing the index, until a `null` element is returned.
 */
@Suppress("FoldInitializerAndIfToElvis")
internal inline fun <E: Any> sqliteList(get: (index: Int) -> E?): List<E> {
    var element: E? = get(0)

    if (element == null) {
        return emptyList()
    }

    val elements = mutableListOf(element)
    var index = 1

    do {
        element = get(index++)
        element?.let(elements::add)
    } while (element != null)

    return elements.toList()
}