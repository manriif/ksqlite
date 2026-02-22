package ksqlite.types

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) {
    internal actual open val intValue: Int
        get() = TODO("Not yet implemented")
}

public actual class Sqlite3LongParam actual constructor(initialValue: Long) {
    public actual val value: Long
        get() = TODO("Not yet implemented")
}

public actual class Sqlite3Utf8Param actual constructor(initialValue: String?) {
    public actual fun readValue(): String? = TODO("Not yet implemented")
}