package ksqlite.kapi.config

import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.kapi.requireConnection

/**
 * Invokes [Logger.log].
 */
internal val LoggerInvoker = SqliteConfigLogCallback { logger: Logger, errorCode, message ->
    logger.log(errorCode, message)
}

/**
 * Invokes [Logger.log].
 */
internal val SqlLoggerInvoker = SqliteConfigSqlLogCallback { logger: SqlLogger, db, event ->
    logger.log(requireConnection(db), event)
}