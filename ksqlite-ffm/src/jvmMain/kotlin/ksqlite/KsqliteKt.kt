package ksqlite

public fun main() {
    val version = sqlite3.sqlite3_libversion().getString(0)
    println("Sqlite3 version = $version")
}