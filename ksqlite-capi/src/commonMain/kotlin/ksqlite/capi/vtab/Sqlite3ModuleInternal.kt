@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.vtab

import ksqlite.capi.memory.ConcurrentMap
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.callbacks.Sqlite3VTabBeginCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabBestIndexCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCloseCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabColumnCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCommitCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabConnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCreateCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabDestroyCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabDisconnectCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabEofCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabFilterCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabFindFunctionCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabIntegrityCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabNextCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabOpenCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabReleaseCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRenameCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRollbackCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRollbackToCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabRowidCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabSavepointCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabShadowNameCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabSyncCallback
import ksqlite.capi.vtab.callbacks.Sqlite3VTabUpdateCallback
import ksqlite.capi.vtab.callbacks.VTabCreateOrConnectFailureResult
import ksqlite.capi.vtab.callbacks.VTabCreateOrConnectSuccessResult
import ksqlite.capi.vtab.callbacks.vTabCreateOrConnectCallbackScope
import ksqlite.capi.vtab.callbacks.Sqlite3VTabCreateOrConnectCallback as CreateOrConnect

///////////////////////////////////////////////////////////////////////////
// Module
///////////////////////////////////////////////////////////////////////////

/**
 * Callbacks of an SQLite 3 Virtual Table module.
 */
internal class VTabModuleCallbacks<AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val create: Sqlite3VTabCreateCallback<AppData, VTab>?,
    val connect: Sqlite3VTabConnectCallback<AppData, VTab>,
    val bestIndex: Sqlite3VTabBestIndexCallback<VTab>,
    val disconnect: Sqlite3VTabDisconnectCallback<VTab>,
    val destroy: Sqlite3VTabDestroyCallback<VTab>,
    val open: Sqlite3VTabOpenCallback<VTab, VTabCursor>,
    val close: Sqlite3VTabCloseCallback<VTabCursor>,
    val filter: Sqlite3VTabFilterCallback<VTabCursor>,
    val next: Sqlite3VTabNextCallback<VTabCursor>,
    val eof: Sqlite3VTabEofCallback<VTabCursor>,
    val column: Sqlite3VTabColumnCallback<VTabCursor>,
    val rowid: Sqlite3VTabRowidCallback<VTabCursor>,
    val update: Sqlite3VTabUpdateCallback<VTab>?,
    val begin: Sqlite3VTabBeginCallback<VTab>?,
    val sync: Sqlite3VTabSyncCallback<VTab>?,
    val commit: Sqlite3VTabCommitCallback<VTab>?,
    val rollback: Sqlite3VTabRollbackCallback<VTab>?,
    val findFunction: Sqlite3VTabFindFunctionCallback<VTab>?,
    val rename: Sqlite3VTabRenameCallback<VTab>?,
    val savepoint: Sqlite3VTabSavepointCallback<VTab>?,
    val release: Sqlite3VTabReleaseCallback<VTab>?,
    val rollbackTo: Sqlite3VTabRollbackToCallback<VTab>?,
    val shadowName: Sqlite3VTabShadowNameCallback?,
    val integrity: Sqlite3VTabIntegrityCallback<VTab>?
) {

    val moduleKind: Sqlite3ModuleKind
        inline get() = when {
            create == null -> Sqlite3ModuleKind.EponymousOnly
            create === connect -> Sqlite3ModuleKind.Eponymous
            else -> Sqlite3ModuleKind.Ordinal
        }
}

/**
 * Holds virtual table callbacks and application data.
 */
internal data class VTabModule<AppData, VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val callbacks: VTabModuleCallbacks<AppData, VTab, VTabCursor>,
    val appData: AppData
)

/**
 * Invokes and returns [block]'s value passing it an instance of [VTabModule] that may be `null`
 * according to [callbacks].
 */
internal inline fun <AppData, R> createVTabModule(
    callbacks: VTabModuleCallbacks<AppData, *, *>?,
    appData: AppData,
    block: (VTabModule<AppData, *, *>?) -> R
): R {
    if (callbacks == null) {
        return block(null)
    }

    return block(VTabModule(callbacks, appData))
}

///////////////////////////////////////////////////////////////////////////
// VTab
///////////////////////////////////////////////////////////////////////////

/**
 * State for virtual table [VTab].
 */
internal class VTabState<VTab : sqlite3_vtab, VTabCursor : sqlite3_vtab_cursor>(
    val vTab: VTab,
    val callbacks: VTabModuleCallbacks<*, VTab, VTabCursor>
) {

    private val cursorStates = ConcurrentMap<Long, VTabCursorState<VTabCursor>>()

    fun cursorState(cursorAddress: Long): VTabCursorState<VTabCursor> {
        return checkNotNull(cursorStates[cursorAddress]) {
            "Virtual table cursor state for address $this was not found"
        }
    }

    inline fun bestIndex(info: sqlite3_index_info) = callbacks.bestIndex
        .handle(vTab, info)

    inline fun disconnect(cleanup: (sqlite3_vtab) -> Unit) = callbacks.disconnect
        .handle(vTab)
        .also { cleanup(vTab) }

    inline fun destroy(cleanup: (sqlite3_vtab) -> Unit) = callbacks.destroy
        .handle(vTab)
        .also { cleanup(vTab) }

    inline fun open(setCursor: (sqlite3_vtab_cursor) -> Unit) {
        TODO()
    }
}

/**
 * Map linking [VTabState]s by the native address of their associated [sqlite3_vtab].
 * This is because there is no way to store an arbitrary object in the native [sqlite3_vtab] struct
 * in most interops.
 */
private val VTabStates = ConcurrentMap<Long, VTabState<*, *>>()

/**
 * Returns the [sqlite3_vtab] for `this` address.
 * This property should be accessed between xCreate/xConnect and xDisconnect/xDestroy.
 */
private inline val Long.vTabState: VTabState<*, *>
    get() = checkNotNull(VTabStates[this]) {
        "Virtual table state for address $this was not found"
    }

/**
 * Invokes `this` create or connect callback.
 */
private inline fun <AppData, VTab : sqlite3_vtab> CreateOrConnect<AppData, VTab>.createOrConnect(
    module: VTabModule<AppData, VTab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVTab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
): Int {
    val callbackResult = vTabCreateOrConnectCallbackScope<VTab>()
        .handle(db, module.appData, argv)

    val result = when (callbackResult) {
        is VTabCreateOrConnectSuccessResult -> {
            setVTab(callbackResult.vTab.apply {
                check(VTabStates.put(address, VTabState(this, module.callbacks)) == null) {
                    "A virtual table instance is already registered with the address $address"
                }
            })

            Sqlite3Result.OK
        }

        is VTabCreateOrConnectFailureResult -> {
            setError(callbackResult.error)
            Sqlite3Result.ERROR
        }
    }

    return result.code
}

/**
 * Invokes the [VTabModuleCallbacks.create].
 */
internal inline fun <AppData, VTab : sqlite3_vtab> vTabCreate(
    module: VTabModule<AppData, VTab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVTab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
): Int = checkNotNull(module.callbacks.create).createOrConnect(module, db, argv, setVTab, setError)

/**
 * Invokes the [VTabModuleCallbacks.connect].
 */
internal inline fun <AppData, VTab : sqlite3_vtab> vTabConnect(
    module: VTabModule<AppData, VTab, *>,
    db: sqlite3,
    argv: Array<String>,
    setVTab: (sqlite3_vtab) -> Unit,
    setError: (String) -> Unit
): Int = module.callbacks.connect.createOrConnect(module, db, argv, setVTab, setError)

/**
 * Invokes the [VTabModuleCallbacks.bestIndex].
 */
internal inline fun vTabBestIndex(
    vTabAddress: Long,
    info: sqlite3_index_info
): Int = vTabAddress.vTabState.bestIndex(info).code

/**
 * Invokes the [VTabModuleCallbacks.disconnect].
 */
internal inline fun vTabDisconnect(
    vTabAddress: Long,
    cleanup: (sqlite3_vtab) -> Unit
): Int = vTabAddress.vTabState.disconnect(cleanup).code

/**
 * Invokes the [VTabModuleCallbacks.destroy].
 */
internal inline fun vTabDestroy(
    vTabAddress: Long,
    cleanup: (sqlite3_vtab) -> Unit
): Int = vTabAddress.vTabState.destroy(cleanup).code

///////////////////////////////////////////////////////////////////////////
// Cursor
///////////////////////////////////////////////////////////////////////////

internal class VTabCursorState<VTabCursor : sqlite3_vtab_cursor>(
    private val cursor: VTabCursor
) {

}

internal inline fun vTabOpen(
    vTabAddress: Long,
    setCursor: (sqlite3_vtab_cursor) -> Unit
): Int {
    TODO()
}