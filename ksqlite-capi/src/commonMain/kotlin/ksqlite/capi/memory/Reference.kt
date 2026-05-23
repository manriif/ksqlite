package ksqlite.capi.memory

/**
 * Reference to an object preventing GC from collecting or moving it.
 */
internal interface Reference<AppData> : Disposable {

    /**
     * Internally referenced data.
     */
    val data: Any?

    /**
     * The associated application data.
     * */
    val appData: AppData

    /**
     * Disposes the reference, making referenced object(s) eligible to GC.
     */
    override fun dispose()
}

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

internal typealias ReferencedData<Data, AppData> = Pair<Data, AppData>

/**
 * Returns `this` [Reference]'s referenced data as [D] paired with the user data.
 */
internal inline fun <reified D : Any, C> Reference<C>.getReferencedData(): ReferencedData<D, C> {
    val data = checkNotNull(data) {
        "No data exists for reference"
    }

    check(data is D) {
        "Data is not of expected type (${data::class} vs ${D::class})"
    }

    return data to appData
}