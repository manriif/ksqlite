# Module Ksqlite Types Internal

Internal module, not intended for public use.

Provides the conversion functions turning the raw integers SQLite hands back through
`ksqlite-foreign` into their typed counterparts declared in [`core`](../core). Each one relies on
a lookup rather than a chain of comparisons, so converting a code back stays cheap no matter how
large the type's value space is: a `when` over the small ones, like
[`convertDbReadonlyResult`](src/commonMain/kotlin/ksqlite/types/internal/EnumResults.kt), and a
precomputed map keyed by code for the largest one,
[`convertResultCode`](src/commonMain/kotlin/ksqlite/types/internal/ResultCodes.kt).