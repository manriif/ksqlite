package ksqlite.capi.types

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) {
    internal actual open val intValue: Int
        get() = TODO("Not yet implemented")
}

public actual class Sqlite3LongParam actual constructor(initialValue: Long) {
    public actual val value: Long
        get() = TODO("Not yet implemented")
}

public actual class Sqlite3StringUtf8Param actual constructor(initialValue: String?) {
    public actual fun readUtf8(): String? = TODO("Not yet implemented")
}