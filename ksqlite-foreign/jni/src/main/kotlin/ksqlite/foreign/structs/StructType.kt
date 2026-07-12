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
package ksqlite.foreign.structs

/**
 * Structs types as recognized in the JNI side.
 */
internal enum class StructType(val type: Int) {
    IndexInfo(0),
    IndexConstraint(1),
    IndexConstraintUsage(2),
    IndexOrderby(3),
    Module(4),
    Vtab(5),
    VtabCursor(6),
    File(7),
    IoMethods(8),
    Vfs(9)
}