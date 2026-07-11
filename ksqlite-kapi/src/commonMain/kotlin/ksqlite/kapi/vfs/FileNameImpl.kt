package ksqlite.kapi.vfs

import ksqlite.capi.sqlite3_filename
import ksqlite.capi.sqlite3_filename_database
import ksqlite.capi.sqlite3_filename_journal
import ksqlite.capi.sqlite3_filename_wal
import ksqlite.capi.sqlite3_uri_boolean
import ksqlite.capi.sqlite3_uri_int64
import ksqlite.capi.sqlite3_uri_key
import ksqlite.capi.sqlite3_uri_parameter

internal class FileNameImpl(private val filename: sqlite3_filename) : FileName {

    override val content: String
        get() = filename.content

    override val databaseFileName: String?
        get() = sqlite3_filename_database(filename)

    override val journalFileName: String?
        get() = sqlite3_filename_journal(filename)

    override val walFileName: String?
        get() = sqlite3_filename_wal(filename)

    override fun getKey(index: Int): String? =
        sqlite3_uri_key(filename, index)

    override fun geValue(parameter: String): String? =
        sqlite3_uri_parameter(filename, parameter)

    override fun geValue(parameter: String, default: Boolean): Boolean =
        sqlite3_uri_boolean(filename, parameter, if (default) 1 else 0) != 0

    override fun geValue(parameter: String, default: Long): Long =
        sqlite3_uri_int64(filename, parameter, default)
}