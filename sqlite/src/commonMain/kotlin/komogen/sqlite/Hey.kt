package komogen.sqlite

import kotlinx.cinterop.toKString

public fun hey() {
    println("sqlite-version = ${sqlite3_version.toKString()}")
}