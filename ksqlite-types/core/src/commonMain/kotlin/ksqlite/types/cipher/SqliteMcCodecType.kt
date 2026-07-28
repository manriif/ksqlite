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
@file:Suppress("SpellCheckingInspection", "ClassName")

package ksqlite.types.cipher

import ksqlite.types.cipher.SqliteMcConfigParam.IntRepresentable
import ksqlite.types.cipher.SqliteMcConfigParam.OfBoolean
import ksqlite.types.cipher.SqliteMcConfigParam.OfByte
import ksqlite.types.cipher.SqliteMcConfigParam.OfEnum
import ksqlite.types.cipher.SqliteMcConfigParam.OfInt
import ksqlite.types.cipher.SqliteMcLegacyPageSize as PageSize

/**
 * Builtin [ciphers](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_overview/)
 */
public sealed class SqliteMcCodecType(override val name: String) : SqliteMcCipher {

    /**
     * [wxSQLite3: AES 128 Bit](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_aes128cbc/)
     */
    public data object AES128 : SqliteMcCodecType("aes128cbc") {

        /**
         * Parameters available for the [AES128] cipher.
         */
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<AES128, Value>

        /**
         * Boolean flag whether the legacy mode should be used.
         */
        public data object LEGACY : Param<Boolean>, OfBoolean("legacy")

        /**
         * Page size to use in legacy mode.
         */
        public data object LEGACY_PAGE_SIZE : Param<PageSize>, PageSize.Param()
    }

    /**
     * [wxSQLite3: AES 256 Bit](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_aes256cbc/)
     */
    public data object AES256 : SqliteMcCodecType("aes256cbc") {

        /**
         * Parameters available for the [AES256] cipher.
         */
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<AES256, Value>

        /**
         * Number of iterations for the key derivation function.
         */
        public data object KDF_ITER : Param<Int>, OfInt("kdf_iter")

        /**
         * Boolean flag whether the legacy mode should be used.
         */
        public data object LEGACY : Param<Boolean>, OfBoolean("legacy")

        /**
         * Page size to use in legacy mode.
         */
        public data object LEGACY_PAGE_SIZE : Param<PageSize>, PageSize.Param()
    }

    /**
     * [sqleet: ChaCha20](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_chacha20/)
     */
    public data object CHACHA20 : SqliteMcCodecType("chacha20") {

        /**
         * Parameters available for the [CHACHA20] cipher.
         */
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<CHACHA20, Value>

        /**
         * Number of iterations for the key derivation function.
         */
        public data object KDF_ITER : Param<Int>, OfInt("kdf_iter")

        /**
         * Boolean flag whether the legacy mode should be used.
         */
        public data object LEGACY : Param<Boolean>, OfBoolean("legacy")

        /**
         * Page size to use in legacy mode.
         */
        public data object LEGACY_PAGE_SIZE : Param<PageSize>, PageSize.Param()

        /**
         * Size of plaintext database header.
         */
        public data object PLAINTEXT_HEADER_SIZE : Param<Int>, OfInt("plaintext_header_size")
    }

    /**
     * [SQLCipher: AES 256 Bit](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_sqlcipher/)
     */
    public data object SQLCIPHER : SqliteMcCodecType("sqlcipher") {

        /**
         * Values allowed for [HMAC_PGNO].
         */
        public enum class Pgno(override val value: Int) : IntRepresentable {
            NATIVE(0),
            LITTLE_ENDIAN(1),
            BIG_ENDIAN(2)
        }

        /**
         * Values allowed for both [KDF_ALGORITHM] and [HMAC_ALGORITHM].
         */
        public enum class HashAlgorithm(override val value: Int) : IntRepresentable {
            SHA1(0),
            SHA256(1),
            SHA512(2)
        }

        /**
         * Values allowed for [PLAINTEXT_HEADER_SIZE].
         */
        public enum class HeaderSize(override val value: Int) : IntRepresentable {
            SIZE_0(0),
            SIZE_16(16),
            SIZE_32(32),
            SIZE_48(48),
            SIZE_64(64),
            SIZE_80(80),
            SIZE_96(96);
        }

        /**
         * Parameters available for the [SQLCIPHER] cipher.
         */
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<SQLCIPHER, Value>

        /**
         * Number of iterations for the key derivation function.
         */
        public data object KDF_ITER : Param<Int>, OfInt("kdf_iter")

        /**
         * Number of iterations for HMAC key derivation.
         */
        public data object FAST_KDF_ITER : Param<Int>, OfInt("fast_kdf_iter")

        /**
         * Flag whether a HMAC should be used.
         */
        public data object HMAC_USE : Param<Boolean>, OfBoolean("hmac_use")

        /**
         * Storage type for page number in HMAC.
         */
        public data object HMAC_PGNO : Param<Pgno>, OfEnum<Pgno>("hmac_pgno", Pgno.entries)

        /**
         * Mask byte for HMAC salt.
         */
        public data object HMAC_SALT_MASK : Param<Byte>, OfByte("hmac_salt_mask")

        /**
         * Page size to use in legacy mode.
         */
        public data object LEGACY_PAGE_SIZE : Param<PageSize>, PageSize.Param()

        /**
         * Hash algoritm for key derivation function.
         */
        public data object KDF_ALGORITHM :
            Param<HashAlgorithm>,
            OfEnum<HashAlgorithm>("kdf_algorithm", HashAlgorithm.entries)

        /**
         * Hash algoritm for HMAC calculation.
         */
        public data object HMAC_ALGORITHM :
            Param<HashAlgorithm>,
            OfEnum<HashAlgorithm>("hmac_algorithm", HashAlgorithm.entries)

        /**
         * Size of plaintext database header.
         */
        public data object PLAINTEXT_HEADER_SIZE :
            Param<HeaderSize>,
            OfEnum<HeaderSize>("plaintext_header_size", HeaderSize.entries)
    }

    /**
     * [System.Data.SQLite: RC4](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_sds_rc4/)
     */
    @Deprecated("The use of this cipher scheme for new applications is strongly discouraged.")
    public data object RC4 : SqliteMcCodecType("rc4") {

        /**
         * Parameters available for the [RC4] cipher.
         */
        @Suppress("DEPRECATION")
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<RC4, Value>

        /**
         * Boolean flag whether the legacy mode should be used.
         */
        public data object LEGACY : Param<Boolean>, OfBoolean("legacy")

        /**
         * Page size to use in legacy mode.
         */
        public data object LEGACY_PAGE_SIZE : Param<PageSize>, PageSize.Param()
    }

    /**
     * [Ascon: Ascon-128 v1.2](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_ascon/)
     */
    public data object ASCON128 : SqliteMcCodecType("ascon128") {

        /**
         * Parameters available for the [ASCON128] cipher.
         */
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<ASCON128, Value>

        /**
         * Number of iterations for the key derivation function.
         */
        public data object KDF_ITER : Param<Int>, OfInt("kdf_iter")

        /**
         * Size of plaintext database header.
         */
        public data object PLAINTEXT_HEADER_SIZE : Param<Int>, OfInt("plaintext_header_size")
    }

    /**
     * [AEGIS: AEGIS family](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_aegis/)
     */
    public data object AEGIS : SqliteMcCodecType("aegis") {

        /**
         * Values allowed for [ALGORITHM].
         */
        public enum class Algorithm(
            override val value: Int,
            public val algorithmName: String
        ) : IntRepresentable {
            AEGIS_128L(1, "aegis-128l"),
            AEGIS_128X2(1, "aegis-128x2"),
            AEGIS_128X4(1, "aegis-128x4"),
            AEGIS_256(1, "aegis-256"),
            AEGIS_256X2(1, "aegis-256x2"),
            AEGIS_256X4(1, "aegis-256x4")
        }

        /**
         * Parameters available for the [] cipher.
         */
        public sealed interface Param<Value : Any> : SqliteMcConfigCipherParam<AEGIS, Value>

        /**
         * Number of iterations for the key derivation with Argon2id.
         */
        public data object TCOST : Param<Int>, OfInt("tcost")

        /**
         * Amount of memory in kB for key derivation with Argon2id.
         */
        public data object MCOST : Param<Int>, OfInt("mcost")

        /**
         * Parallelism, number of threads for key derivation with Argon2.
         */
        public data object PCOST : Param<Int>, OfInt("pcost")

        /**
         * AEGIS variant to be used for page encryption.
         */
        public data object ALGORITHM :
            Param<Algorithm>,
            OfEnum<Algorithm>("algorithm", Algorithm.entries)

        /**
         * Size of plaintext database header.
         */
        public data object PLAINTEXT_HEADER_SIZE : Param<Int>, OfInt("plaintext_header_size")
    }
}