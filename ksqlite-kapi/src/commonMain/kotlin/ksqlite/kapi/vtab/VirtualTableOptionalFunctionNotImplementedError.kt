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
package ksqlite.kapi.vtab

/**
 * Thrown when the [VirtualTableModule] that produced a [VirtualTable] declared [function] in its
 * [VirtualTableModule.optionalFunctions], but the [VirtualTable] does not override the method
 * [function] represents.
 */
public class VirtualTableOptionalFunctionNotImplementedError internal constructor(
    public val function: VirtualTableOptionalFunction
) : Error(
    "${function.name} is listed in the module's optionalFunctions, but the VirtualTable it " +
        "produced does not override the corresponding method."
)
