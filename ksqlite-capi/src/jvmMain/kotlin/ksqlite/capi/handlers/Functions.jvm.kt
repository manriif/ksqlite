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
package ksqlite.capi.handlers

import ksqlite.capi.ApplicationDefinedFunction
import ksqlite.capi.memory.toArrayOrEmpty
import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_value
import ksqlite.foreign.sqlite3.sqlite3_user_data
import ksqlite.foreign.`sqlite3_create_function_v2$xFinal`
import ksqlite.foreign.`sqlite3_create_function_v2$xFunc`
import ksqlite.foreign.`sqlite3_create_function_v2$xStep`
import ksqlite.foreign.`sqlite3_create_window_function$xInverse`
import ksqlite.foreign.`sqlite3_create_window_function$xValue`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Base for function [Handler]s.
 */
internal abstract class FunctionHandler : Handler() {

    /**
     * Handler for function callback.
     */
    protected inline fun handleFunction(
        context: MemorySegment,
        call: ApplicationDefinedFunction<*>.(sqlite3_context) -> Unit
    ) {
        handle(sqlite3_user_data(context)) { function: ApplicationDefinedFunction<*>, _ ->
            function.call(sqlite3_context(context))
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// 1 arg
///////////////////////////////////////////////////////////////////////////

/**
 * Handler for the `final` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionFinalHandler :
    FunctionHandler(),
    `sqlite3_create_function_v2$xFinal`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_function_v2$xFinal`.allocate(this, arena)

    override fun apply(context: MemorySegment) =
        handleFunction(context, ApplicationDefinedFunction<*>::callFinal)
}

/**
 * Handler for the `value` argument of  [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionValueHandler :
    FunctionHandler(),
    `sqlite3_create_window_function$xValue`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_window_function$xValue`.allocate(this, arena)

    override fun apply(context: MemorySegment) =
        handleFunction(context, ApplicationDefinedFunction<*>::callValue)
}

///////////////////////////////////////////////////////////////////////////
// 3 args
///////////////////////////////////////////////////////////////////////////

/**
 * Base for 3-args function [Handler]s.
 */
internal abstract class Function3ArgsHandler : FunctionHandler() {

    /**
     * Handler for 3-args function callback.
     */
    protected fun handleFunction(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment,
        call: ApplicationDefinedFunction<*>.(sqlite3_context, Array<sqlite3_value>) -> Unit
    ) = handleFunction(context) { context ->
        call(context, argv.toArrayOrEmpty(argc, ::sqlite3_value))
    }
}

/**
 * Handler for the `func` argument of [ksqlite.capi.sqlite3_create_function] and
 * [ksqlite.capi.sqlite3_create_function_v2].
 */
internal class FunctionFuncHandler :
    Function3ArgsHandler(),
    `sqlite3_create_function_v2$xFunc`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_function_v2$xFunc`.allocate(this, arena)

    override fun apply(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callFunc)
}

/**
 * Handler for the `step` argument of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionStepHandler :
    Function3ArgsHandler(),
    `sqlite3_create_function_v2$xStep`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_function_v2$xStep`.allocate(this, arena)

    override fun apply(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callStep)
}

/**
 * Handler for the `inverse` argument of [ksqlite.capi.sqlite3_create_window_function].
 */
internal class FunctionInverseHandler :
    Function3ArgsHandler(),
    `sqlite3_create_window_function$xInverse`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_create_window_function$xInverse`.allocate(this, arena)

    override fun apply(
        context: MemorySegment,
        argc: Int,
        argv: MemorySegment
    ) = handleFunction(context, argc, argv, ApplicationDefinedFunction<*>::callInverse)
}