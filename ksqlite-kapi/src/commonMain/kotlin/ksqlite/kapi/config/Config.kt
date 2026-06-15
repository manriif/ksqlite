package ksqlite.kapi.config

import ksqlite.kapi.buffer.Buffer
import ksqlite.types.SqliteConfigOption

public typealias Config = SqliteConfigOption<Buffer, Nothing, Logger, SqlLogger>