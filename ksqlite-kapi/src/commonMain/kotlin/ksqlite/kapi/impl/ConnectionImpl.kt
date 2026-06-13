package ksqlite.kapi.impl

import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.kapi.Connection

internal class ConnectionImpl(private val db: sqlite3): Connection {

    override fun close() {

    }
}