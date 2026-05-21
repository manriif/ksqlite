package ksqlite.capi.memory

/**
 * Reference to an object preventing GC from collecting or moving it.
 */
internal interface Reference<ClientData> : Disposable {

    /**
     * Internally referenced data.
     */
    val data: Any?

    /**
     * The associated client data.
     * */
    val clientData: ClientData

    /**
     * Disposes the reference, making referenced object(s) eligible to GC.
     */
    override fun dispose()
}

internal typealias ReferencedData<Data, ClientData> = Pair<Data, ClientData>

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

    return data to clientData
}