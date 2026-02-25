package ksqlite.capi.memory

/**
 * Base for pointer of sqlite3_* types.
 */
public abstract class Pointer<Pointer : Any> internal constructor(
    internal val pointer: Pointer,
    restricted: Boolean
) {

    private var _memory = lazy {
        check(!restricted) {
            "This pointer is limited and some sqlite APIs cannot be called in a callback function" +
                    "because of not yet resolved memory management concerns."
        }

        MemoryManager()
    }

    internal val memory by _memory

    /**
     * Releases the resources associated with pointer.
     */
    internal fun dispose() {
        if (_memory.isInitialized()) {
            memory.close()
        }
    }
    /*
        /**
         * Invokes [block] letting SQLite allocating memory to hold [Struct].
         */
        internal fun allocate(block: (PointerPointer) -> Int): Sqlite3Result {
            val currentState = state

            if (currentState != State.Created) {
                error(
                    when (currentState) {
                        is State.Allocated -> if (currentState.pointer == null) {
                            "The struct has already been allocated"
                        } else {
                            "The struct allocation failed and it is not safe to allocate it again, " +
                                    "check the SQLite documentation to know what to do next"
                        }

                        State.Deallocated -> "The struct has been deallocated, this pointer instance " +
                                "should no longer be used"
                    }
                )
            }

            var pointer: Pointer? = null
            val result = convertResult(doAllocation(block) { pointer = it })

            if (result == Sqlite3Result.OK) {
                state = State.Allocated(checkNotNull(pointer) {
                    "Unexpected null pointer value while SQLite succeeded"
                })
            } else {
                check(pointer == null) { "Unexpected pointer value while SQLite failed" }
                state = State.Allocated(null)
            }

            return result
        }

        /**
         * Invokes [block] to deallocate memory previously allocated by SQLite.
         * If a previous attempt failed, the pointer passed to [block] is `null`.
         * If the deallocation fails, the state of the pointer stays unchanged.
         *
         * If accessed and deallocation succeed, [memory] is closed.
         */
        internal fun deallocate(block: (Pointer?) -> Int): Sqlite3Result {
            val currentState = state

            if (currentState !is State.Allocated) {
                error(
                    when (currentState) {
                        is State.Created -> "The struct must be allocated first, which was not done"
                        State.Deallocated -> "The struct has already been deallocated, this pointer " +
                                "instance should no longer be used"
                    }
                )
            }

            val result = convertResult(block(currentState.pointer))

            if (result == Sqlite3Result.OK) {
                if (_memory.isInitialized()) {
                    memory.close()
                }

                state = State.Deallocated
            }

            return result
        }*/
}