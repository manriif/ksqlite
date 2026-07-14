/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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