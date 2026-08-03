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
package ksqlite.kapi.helpers

/**
 * Lists elements obtained from [get], incrementing the index, until a `null` element is returned.
 */
@Suppress("FoldInitializerAndIfToElvis")
internal inline fun <E : Any> sqliteList(get: (index: Int) -> E?): List<E> {
    var element: E? = get(0)

    if (element == null) {
        return emptyList()
    }

    val elements = mutableListOf(element)
    var index = 1

    do {
        element = get(index++)
        element?.let(elements::add)
    } while (element != null)

    return elements.toList()
}