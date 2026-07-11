package ksqlite.kapi.function

import ksqlite.kapi.value.ValueReturnScope
import ksqlite.kapi.value.ValueReturnScopeImpl

/**
 * Scope for use with [ScalarFunction.func].
 */
public class ScalarFunctionFuncScope internal constructor(scope: FunctionScopeImpl) :
    FunctionScope by scope,
    ValueReturnScope by ValueReturnScopeImpl(scope),
    AuxDataScope(scope)