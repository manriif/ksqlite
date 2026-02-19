package sqlite

import kotlin.js.JsNumber

public interface Wasm {

    public val bigIntEnabled: Boolean

    public fun alloc(byteCount: JsNumber)

    public fun allocCString()
    /*allocFromTypedArray()
    allocMainArgv()
    allocPtr()
    bigIntEnabled = true
    cArgvToJs()
    compileOptionUsed()
    cstrlen()
    cstrncpy()
    cstrToJs()
    dealloc()
    exports [object]
    functionEntry()
    functionTable()
    getMemValue()
    getPtrValue()
    heap16()
    heap16u()
    heap32()
    heap32u()
    heap8()
    heap8u()
    heapForSize()
    installFunction()
    isPtr()
    isPtr32()
    isPtr64()
    isSharedTypedArray()
    jsFuncToWasm()
    jstrcpy()
    jstrlen()
    jstrToUintArray()
    memory [object]
    peek()
    peek16()
    peek32()
    peek32f()
    peek64()
    peek64f()
    peek8()
    peekPtr()
    poke()
    poke16()
    poke32()
    poke32f()
    poke64()
    poke64f()
    poke8()
    pokePtr()
    pstack [object]
    ptr [object]
    realloc()
    scopedAlloc()
    scopedAllocCall()
    scopedAllocCString()
    scopedAllocMainArgv()
    scopedAllocPop()
    scopedAllocPtr()
    scopedAllocPush()
    scopedInstallFunction()
    setMemValue()
    setPtrValue()
    sizeofIR()
    typedArrayPart()
    typedArrayToString()
    uninstallFunction()
    xCall()
    xCallWrapped()
    xGet()
    xWrap()*/
}