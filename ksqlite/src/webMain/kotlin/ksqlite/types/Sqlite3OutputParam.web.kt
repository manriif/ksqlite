package ksqlite.types

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) {
    internal actual open val rawValue: Int
        get() = TODO("Not yet implemented")
}