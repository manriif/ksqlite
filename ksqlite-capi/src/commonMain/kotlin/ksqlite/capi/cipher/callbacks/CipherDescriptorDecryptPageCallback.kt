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
package ksqlite.capi.cipher.callbacks

import ksqlite.capi.memory.Buffer
import ksqlite.types.SqliteResultCode

/**
 * This function is used to decrypt a single database page.
 *
 * [CipherDescriptor](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#cipher-descriptor)
 */
public fun interface CipherDescriptorDecryptPageCallback<Cipher : Any> {

    /**
     * Details on parameters and result can be found [here](https://utelle.github.io/SQLite3MultipleCiphers/docs/ciphers/cipher_dynamic/#cipher-descriptor).
     */
    public fun apply(
        cipher: Cipher,
        page: Int,
        data: Buffer,
        reserved: Int,
        hmacCheck: Int
    ): SqliteResultCode.OkOrFailure
}