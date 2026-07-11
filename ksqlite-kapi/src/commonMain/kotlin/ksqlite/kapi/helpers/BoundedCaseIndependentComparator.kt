package ksqlite.kapi.helpers

import ksqlite.capi.sqlite3_strnicmp

/**
 * Comparator using [sqlite3_strnicmp].
 */
internal class BoundedCaseIndependentComparator(private val maxBytes: Int) : Comparator<String> {

    override fun compare(a: String, b: String): Int = sqlite3_strnicmp(a, b, maxBytes)
}