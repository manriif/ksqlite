# Module Ksqlite Structs

Struct memory-layout machinery for `ksqlite-foreign/jni` and `ksqlite-foreign/wasm`, the two
targets that don't get one for free from their own interop tooling, unlike Kotlin/Native cinterop
or Java FFM's jextract.

`Struct<Type, Member, Pointer>` wraps a `Memory<Pointer>` pointing at a C struct, reading and
writing a member at the offset and size its `StructLayout` (an `IntArray` of offset/size pairs,
one per member, plus the struct's own total size) reports for it. `Adapter<Pointer>` is the only
platform-specific piece, allocating or reinterpreting that `Memory` and moving raw bytes through
it, `ksqlite-foreign/jni` backs it with a `ByteBuffer`, `ksqlite-foreign/wasm` with a `DataView`
into the WASM heap.

A layout itself still has to come from somewhere. `setStructLayoutProvider()` plugs in the one
piece this module doesn't provide on its own, resolved through `ksqlite_struct_layout_allocate()`,
a small C helper computing every member's real `offsetof()` on the target ABI, so a layout always
matches how the C compiler actually laid the struct out, never a hand-copied guess.
