package ksqlite.types

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.cstr
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.interpretNullablePointed
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value

/**
 * Base for output parameter.
 */
public abstract class Sqlite3Param<Type, Var : CPointed> internal constructor(initialValue: Type) {

    private var nativeVar: Var? = null
    private var lastValue: Type = initialValue

    protected abstract val Var.memValue: Type

    protected val currentValue: Type
        get() = nativeVar?.memValue ?: lastValue

    /**
     * Allocates memory and initializes with [value].
     */
    protected abstract fun NativePlacement.allocate(value: Type): Var

    /**
     * Allocates memory into [placement] and returns the pointer to the allocated [Var].
     */
    internal fun attach(placement: NativePlacement): CPointer<Var> {
        check(nativeVar == null) { "Param is already attached" }
        val allocated = placement.allocate(lastValue)
        nativeVar = allocated
        return allocated.ptr
    }

    /**
     * Invalidates the previously allocated [Var].
     */
    internal fun detach() {
        lastValue = checkNotNull(nativeVar) { "Param is not attached" }.memValue
        nativeVar = null
    }
}

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

public actual typealias PointerType = CPointed

public actual class Sqlite3PointerParam<Pointer: PointerType> actual constructor():
    Sqlite3Param<Pointer?, CPointerVar<CPointed>>(null) {

    public actual val value: Pointer?
        get() = currentValue

    override val CPointerVar<CPointed>.memValue: Pointer?
        get() = TODO("Not yet implemented")

    override fun NativePlacement.allocate(value: Pointer?): CPointerVar<CPointed> {
        return allocPointerTo()
    }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

public actual class Sqlite3Utf8Param actual constructor(initialValue: String?) :
    Sqlite3Param<String?, CPointerVar<ByteVar>>(initialValue) {

    public actual fun readValue(): String? = currentValue

    override val CPointerVar<ByteVar>.memValue: String?
        get() = this.value?.toKStringFromUtf8()

    override fun NativePlacement.allocate(value: String?): CPointerVar<ByteVar> {
        return allocPointerTo<ByteVar>().apply {
            this.value = value?.cstr?.run {
                place(interpretCPointer(alloc(size, align).rawPtr)!!)
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Primitives
///////////////////////////////////////////////////////////////////////////

public actual open class Sqlite3IntBaseParam internal actual constructor(initialValue: Int) :
    Sqlite3Param<Int, IntVar>(initialValue) {

    internal actual open val intValue: Int
        get() = currentValue

    override val IntVar.memValue: Int
        get() = value

    override fun NativePlacement.allocate(value: Int): IntVar {
        return alloc(value)
    }
}

public actual class Sqlite3LongParam actual constructor(initialValue: Long) :
    Sqlite3Param<Long, LongVar>(initialValue) {

    public actual val value: Long
        get() = currentValue

    override val LongVar.memValue: Long
        get() = value

    override fun NativePlacement.allocate(value: Long): LongVar {
        return alloc(value)
    }
}