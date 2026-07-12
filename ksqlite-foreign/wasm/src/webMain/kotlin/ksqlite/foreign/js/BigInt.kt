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
@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.foreign.js

import kotlin.js.JsBigInt
import kotlin.js.toJsBigInt
import kotlin.js.toLong

/**
 * Returns a [JsBigInt] which is the sum of `this` + [value].
 */
public operator fun JsBigInt.plus(value: Int): JsBigInt = (toLong() + value).toJsBigInt()

/**
 * Returns a [JsBigInt] which is the sum of `this` + [value].
 */
@Suppress("Re")
public operator fun JsBigInt.plus(value: Long): JsBigInt = (toLong() + value).toJsBigInt()