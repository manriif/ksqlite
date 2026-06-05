package ksqlite.capi.vtab

import ksqlite.structs.sqlite3_index_info
import ksqlite.structs.sqlite3_module
import ksqlite.structs.sqlite3_vtab
import ksqlite.structs.sqlite3_vtab_cursor

internal typealias s3_index_info = sqlite3_index_info
internal typealias s3_module = sqlite3_module
internal typealias s3_vtab = sqlite3_vtab
internal typealias s3_vtab_cursor = sqlite3_vtab_cursor