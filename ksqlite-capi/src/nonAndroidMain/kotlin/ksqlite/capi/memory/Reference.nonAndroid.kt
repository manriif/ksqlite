package ksqlite.capi.memory

/**
 * Holds an instance of [Data] and [AppData]
 */
internal interface DataHolder<Data, AppData> {

    /**
     * Internally referenced data.
     */
    val data: Data

    /**
     * The associated application data.
     * */
    val appData: AppData
}

/**
 * Keeps a strong reference to [data] and [appData] allowing future access.
 *
 * Data is stored as [Any]? to reduce the number of generic types across files.
 * Use [cast] to retrieve the [DataHolder] with the expected type.
 */
internal interface Reference<AppData> :
    DataHolder<Any?, AppData>,
    Disposable {

    /**
     * Disposes the reference, making referenced object(s) eligible to GC.
     */
    override fun dispose(callDestructor: Boolean)
}

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns `this` [Reference]'s referenced data as [D] paired with the user data.
 */
internal inline fun <reified D : Any, AppData> Reference<AppData>.cast(): DataHolder<D, AppData> {
    val data = checkNotNull(data) {
        "No data exists for reference"
    }

    check(data is D) {
        "Data is not of expected type (${data::class} vs ${D::class})"
    }

    @Suppress("UNCHECKED_CAST")
    return this as DataHolder<D, AppData>
}