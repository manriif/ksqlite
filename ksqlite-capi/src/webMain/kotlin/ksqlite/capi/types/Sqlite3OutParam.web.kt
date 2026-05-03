package ksqlite.capi.types

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) {
    internal actual open val intValue: Int
        get() = TODO("Not yet implemented")
}

public actual class Sqlite3LongOutParam actual constructor(initialValue: Long) {
    public actual val value: Long
        get() = TODO("Not yet implemented")
}

public actual class Sqlite3Utf8OutParam actual constructor(initialValue: String?) {
    public actual fun readUtf8(): String? = TODO("Not yet implemented")
}