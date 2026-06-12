package ksqlite.kapi.impl

import ksqlite.capi.types.sqlite3
import ksqlite.kapi.Connection

internal class ConnectionImpl(private val db: sqlite3): Connection {
}