@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import org.sqlite.jni.capi.CApi

public actual fun sqlite3_libversion(): String {
    return CApi.sqlite3_libversion()
}