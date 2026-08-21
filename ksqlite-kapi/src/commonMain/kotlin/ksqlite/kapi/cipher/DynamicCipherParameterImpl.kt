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
package ksqlite.kapi.cipher

import ksqlite.capi.cipher.CipherParams

/**
 * Wrapper for [CipherParams].
 */
internal class DynamicCipherParameterImpl(private val params: CipherParams) :
    DynamicCipherParameter {
    override var m_name: String by params::m_name
    override var m_value: Int by params::m_value
    override var m_default: Int by params::m_default
    override var m_minValue: Int by params::m_minValue
    override var m_maxValue: Int by params::m_maxValue
}