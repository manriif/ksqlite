@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite

import kotlinx.cinterop.ExperimentalForeignApi

public actual typealias pointer = Long

public actual typealias sqlite3_stmt = Long