package ksqlite

class Ksqlite {

    companion object {

        init {
            System.loadLibrary("sqlite3")
        }
    }
}