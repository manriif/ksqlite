@file:OptIn(ExperimentalForeignApi::class)

package ksqlite

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString

public actual val sqliteLibVersion: String
    get() = sqlite3_libversion()!!.toKString()