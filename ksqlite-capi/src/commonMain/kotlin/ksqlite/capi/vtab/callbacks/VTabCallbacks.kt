package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCreateOrConnectCallback as CreateOrConnect

///////////////////////////////////////////////////////////////////////////
// Create or connect
///////////////////////////////////////////////////////////////////////////

/**
 * Success result for [CreateOrConnect].
 */
internal data class VTabCreateOrConnectSuccessResult<VTab : sqlite3_vtab>(val vTab: VTab) :
    CreateOrConnect.Result<VTab>

/**
 * Failure result for [CreateOrConnect].
 */
internal data class VTabCreateOrConnectFailureResult(val error: String) :
    CreateOrConnect.Result<Nothing>

/**
 * Implementation of [CreateOrConnect.Scope].
 */
private object VTabCreateOrConnectCallbackScope :
    CreateOrConnect.Scope<sqlite3_vtab> {

    override fun success(vTab: sqlite3_vtab): CreateOrConnect.Result<sqlite3_vtab> {
        return VTabCreateOrConnectSuccessResult(vTab)
    }

    override fun failure(error: String): CreateOrConnect.Result<sqlite3_vtab> {
        return VTabCreateOrConnectFailureResult(error)
    }
}

/**
 * Returns [VTabCreateOrConnectCallbackScope].
 * The same instance is always returned to reduce allocation.
 */
@Suppress("UNCHECKED_CAST")
internal fun <VTab : sqlite3_vtab> vTabCreateOrConnectCallbackScope(): CreateOrConnect.Scope<VTab> {
    return VTabCreateOrConnectCallbackScope as CreateOrConnect.Scope<VTab>
}