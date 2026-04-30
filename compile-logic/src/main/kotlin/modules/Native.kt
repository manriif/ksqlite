package modules

import SQLITE3

/**
 * Definition file noStringConversions.
 */
val KsqliteNoStringConversions = listOf(
    "bind_pointer",
    "bind_text",
    "bind_text64",
    "blob_open",
    "exec",
    "keyword_check",
    "open",
    "open_v2",
    "prepare_v2",
    "prepare_v3",
    "result_error",
    "result_pointer",
    "result_text",
    "result_text64",
    "serialize",
    "table_column_metadata",
    "wal_checkpoint_v2"
).map { function ->
    "${SQLITE3}_$function"
}