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