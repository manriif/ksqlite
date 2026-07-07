package ksqlite.capi.vtab

/**
 * Returns a unique name for a function handler given its identifying arguments.
 */
internal fun vTabFunctionKey(name: String, nArg: Int): String {
    return "vTab_function_${name}_${nArg}"
}