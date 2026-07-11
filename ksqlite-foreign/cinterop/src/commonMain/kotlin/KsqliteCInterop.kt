/**
 * This does strictly nothing, but it is required for Kotlin to produce a .klib otherwise publishing
 * fails. It seems like no .klib is generated when there is no source files.
 */
public fun ksqliteLoadLibrary(): Unit = Unit