@file:Suppress("RemoveExplicitTypeArguments")

package ksqlite.foreign.structs

import ksqlite.foreign.js.getMember
import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsAny
import kotlin.js.unsafeCast
import kotlin.reflect.KProperty1

/**
 * JS-wrapper around a C-Struct.
 */
public external interface StructType : JsAny {

    /**
     * Pointer to the struct address in the WASM heap.
     */
    public val pointer: WasmPointer

    /**
     * Disposes the struct.
     */
    public fun dispose()
}

/**
 * Struct's member detail.
 */
public external interface StructMember : JsAny {

    /**
     * Member key.
     */
    public val key: String

    /**
     * Member property name.
     */
    public val name: String

    /**
     * Offset of the member in the struct.
     */
    public val offset: Int

    /**
     * Member type signature.
     */
    public val signature: String

    /**
     * Size of the member in the struct?
     */
    public val size: Int
}

/**
 * Information on a struct.
 */
public external interface StructInfo<@Suppress("unused") S> : JsAny {

    /**
     * Members of the struct.
     */
    public val members: JsAny
}

/**
 * Returns the [StructMember] for the [property] of [S].
 */
public fun <S : StructType> StructInfo<S>.member(property: KProperty1<S, *>): StructMember =
    checkNotNull(getMember(members, property.name)).unsafeCast<StructMember>()

/**
 * Constructor function to the struct [S].
 */
public external interface StructCtor<S : StructType> : JsAny {

    /**
     * Information of the struct [S].
     */
    public val structInfo: StructInfo<S>
}

/**
 * Instantiate a new instance of [S].
 */
@JsFun("(ctor) => new ctor()")
public external fun <S : StructType> instantiate(ctor: StructCtor<S>): S

/**
 * Instantiate a new instance of [S].
 */
public operator fun <S : StructType> StructCtor<S>.invoke(): S =
    instantiate(this)

/**
 * Instantiate a new instance of [S] wrapping C-struct [pointer].
 */
@JsFun("(ctor, pointer) => new ctor(pointer)")
public external fun <S : StructType> instantiate(
    ctor: StructCtor<S>,
    pointer: WasmPointer
): S

/**
 * Instantiate a new instance of [S] wrapping C-struct [pointer].
 */
public operator fun <S : StructType> StructCtor<S>.invoke(pointer: WasmPointer): S =
    instantiate(this, pointer)