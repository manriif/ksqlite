#include <cstdlib>
#include <cstring>
#include <limits>
#include <jni.h>

#include "ksqlite.h"
#include "utils/Unicode.h"

#include <unordered_map>

// FIXME I do not know if the IDE is broken but this is declared in <jni.h> but IDE complain that
//  it does not exists
typedef uint8_t jboolean;

///////////////////////////////////////////////////////////////////////////
// Classes
///////////////////////////////////////////////////////////////////////////

#define KSQLITE_JNI_EXCEPTION "KsqliteJniException"
#define DESTRUCTOR_CALLBACK "DestructorCallback"
#define AUTO_EXTENSION_CALLBACK "AutoExtensionCallback"
#define AUTO_VACUUM_PAGES_CALLBACK "AutoVacuumPagesCallback"

///////////////////////////////////////////////////////////////////////////
// Exceptions
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes JNI FatalError.
 *
 * For now this is used wherever a coding error is made or when an out of memory error occur.
 * This behaviour may change in the future and all functions calling this may be reimplemented to
 * handle things correctly.
 */
#define FatalError(M) env->FatalError(M)

/**
 * Raises a fatal error if pointer is not null.
 */
static inline void requireNull(
    JNIEnv* const env,
    void* pointer
) {
    if (pointer != nullptr) {
        FatalError("Expected object to be null");
    }
}

/**
 * Raises a fatal error if pointer is null.
 */
static inline void* requireNonNull(
    JNIEnv* const env,
    void* pointer
) {
    if (pointer == nullptr) {
        FatalError("Expected object not to be null");
    }

    return pointer;
}

/**
 * Raises a fatal error if object is null.
 */
static inline jobject requireNonNullJobject(
    JNIEnv* const env,
    jobject object
) {
    return static_cast<jobject>(requireNonNull(env, object));
}

#define RequireNull(P) requireNull(env, (P))
#define RequireNonNull(P) requireNonNull(env, (P))
#define RequireNonNullJobject(O) requireNonNullJobject(env, (O))

/**
 * Raises a fatal error if there is a pending exception.
 */
static inline void exceptionClearAndAbort(
    JNIEnv* const env,
    const char* errorMessage
) {
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        FatalError(errorMessage);
    }
}

#define ExceptionClearAndAbort(M) exceptionClearAndAbort(env, (M))

/**
 * Raises a fatal error if expression E is false (due to a lack of memory).
 *
 * TODO do not throw, all out of memory checks must be handled correctly to align with other
 *  platform's behaviour.
 */
#define OutOfMemoryCheck(E) if (!(E)) FatalError("KSQLite JNI is out of memory.")

/**
 * Returns true if an exception has been reported.
 */
static inline bool reportException(
    JNIEnv* const env,
    bool clear
) {
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();

        if (clear) {
            env->ExceptionClear();
        }

        return true;
    }

    return false;
}

#define IfExceptionThrownClear(clear) if (reportException(env, clear))
#define IfExceptionThrown IfExceptionThrownClear(false)
#define IfExceptionNotThrownClear(clear) if (!reportException(env, clear))
#define IfExceptionNotThrown IfExceptionNotThrownClear(false)

///////////////////////////////////////////////////////////////////////////
// JNI reference management helpers
///////////////////////////////////////////////////////////////////////////

static jobject newGlobalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object == nullptr) {
        return nullptr;
    }

    const auto ref = env->NewGlobalRef(object);
    OutOfMemoryCheck(ref != nullptr);
    return ref;
}

static jobject newLocalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object == nullptr) {
        return nullptr;
    }

    const auto ref = env->NewLocalRef(object);
    OutOfMemoryCheck(ref != nullptr);
    return ref;
}

static inline void deleteGlobalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object != nullptr) {
        env->DeleteGlobalRef(object);
    }
}

static inline void deleteLocalReference(
    JNIEnv* const env,
    jobject object
) {
    if (object != nullptr) {
        env->DeleteLocalRef(object);
    }
}

#define GlobalRefCreate(O) newGlobalReference(env, (O))
#define LocalRefCreate(O) newLocalReference(env, (O))
#define GlobalRefDestroy(O) deleteGlobalReference(env, (O))
#define LocalRefDestroy(O) deleteLocalReference(env, (O))

///////////////////////////////////////////////////////////////////////////
// JNI classes, methods, fields helpers
///////////////////////////////////////////////////////////////////////////

static inline jclass getClassOrDie(
    JNIEnv* const env,
    const char* name,
    const char* errorMessage
) {
    const auto klass = GlobalRefCreate(env->FindClass(name));
    ExceptionClearAndAbort(errorMessage);
    return reinterpret_cast<jclass>(klass);
}

#define RequireClass(klassName) \
    getClassOrDie(env, klassName, "Error getting reference to " klassName " class")

#define RequireKsqliteClass(klassName) RequireClass("ksqlite/" klassName)

/**
 * Raises a fatal error when a method was not found on a given class.
 */
static inline jmethodID getMethodIdOrDie(
    JNIEnv* const env,
    jclass klass,
    const char* name,
    const char* signature,
    const char* errorMessage
) {
    const auto methodId = env->GetMethodID(klass, name, signature);
    ExceptionClearAndAbort(errorMessage);
    return methodId;
}

#define RequireClassMethod(klass, name, signature, className) \
    getMethodIdOrDie(env, klass, name, signature, \
    "Error getting reference to " className "#" name " method")

#define RequireMethod(O, name, signature, className) \
    RequireClassMethod(O.klass, name, signature, className)

#define RequireKsqliteClassMethod(klass, name, signature, className) \
    RequireClassMethod(klass, name, signature, "ksqlite." className)

#define RequireKsqliteMethod(O, name, signature, className) \
    RequireKsqliteClassMethod(O.klass, name, signature, className)

///////////////////////////////////////////////////////////////////////////
// Mutex
///////////////////////////////////////////////////////////////////////////

/**
 * Common type for object guarded by a mutex.
 */
struct MutexGuarded {
    sqlite3_mutex* mutex;
};

#define MutexAllocate(O) \
    O.mutex = sqlite3_mutex_alloc(SQLITE_MUTEX_FAST); \
    OutOfMemoryCheck(O.mutex)

#define MutexDestroy(O) sqlite3_mutex_free(O.mutex)
#define MutexEnter(O) sqlite3_mutex_enter(O.mutex)
#define MutexLeave(O) sqlite3_mutex_leave(O.mutex)

///////////////////////////////////////////////////////////////////////////
// Destructor
///////////////////////////////////////////////////////////////////////////

/**
 * Destructor function type.
 */
typedef void(* DestructorFunction)(void*);

/**
 * Common type for objects which can be destroyed.
 */
struct Destroyable {
    jobject destructor;
};

///////////////////////////////////////////////////////////////////////////
// Handler
///////////////////////////////////////////////////////////////////////////

/**
 * Common type for callback handlers.
 */
struct Handler : MutexGuarded {
    jobject callback;
    jmethodID call;
};

/**
 * Common type for callback handlers with a destructor.
 */
struct DestroyableHandler : Handler, Destroyable {
};

/**
 * Configures a handler callback.
 */
#define HandlerConfigure(handler, callback, signature, className) \
    const auto klass = env->GetObjectClass(callback); \
    handler.call = RequireKsqliteClassMethod(klass, "call", signature, className); \
    handler.callback = GlobalRefCreate(callback); \
    LocalRefDestroy(klass)

/**
 * Configures a handler callback and the associated destructor.
 */
#define HandlerWithDestructorConfigure(handler, destructor, callback, signature, className) \
    if (destructor != nullptr) handler.destructor = GlobalRefCreate(destructor);            \
    HandlerConfigure(handler, callback, signature, className)                               \

/**
 * Declares the common variables for handler callback invocation.
 * The callback is wrapped into a local reference that can be released when no longer required.
 */
#define HandlerCallbackConsume(handler) \
    MutexEnter(handler); \
    const auto callback = LocalRefCreate(RequireNonNullJobject(handler.callback)); \
    const auto call = handler.call; \
    MutexLeave(handler)

///////////////////////////////////////////////////////////////////////////
// Holders
///////////////////////////////////////////////////////////////////////////

/**
 * Holder for an optional pointer allocated with sqlite3_malloc(), an optional Java object to keep
 * globally reachable and an optional globally referenced destructor.
 */
struct Freeable : Destroyable {
    void* pointer;
    jobject target;
};

/**
 * Allocates a new Freeable if at least one of pointer, target or destructor is not null. Returns
 * null if none of the supplied arguments is not null.
 */
static Freeable* allocateFreeable(
    JNIEnv* env,
    void* pointer,
    jobject target,
    jobject destructor
) {
    if (pointer == nullptr && target == nullptr && destructor == nullptr) {
        return nullptr;
    }

    jobject globalTarget = nullptr;
    jobject globalDestructor = nullptr;

    if (target != nullptr) {
        globalTarget = GlobalRefCreate(target);
    }

    if (destructor != nullptr) {
        globalDestructor = GlobalRefCreate(destructor);
    }

    return new Freeable { { globalDestructor }, pointer, globalTarget };
}

#define AllocateFreeable(pointer, target, destructor) \
    allocateFreeable(env, pointer, target, destructor)

#define AllocateFreeablePointer(pointer, destructor) \
    AllocateFreeable(pointer, nullptr, destructor)

#define AllocateFreeableTarget(target, destructor) \
    AllocateFreeable(nullptr, target, destructor)

///////////////////////////////////////////////////////////////////////////
// Global State
///////////////////////////////////////////////////////////////////////////

/**
 * Common type for java classes.
 */
struct Class {
    jclass klass;
};

/**
 * Global state.
 */
static struct {
    JavaVM* jvm = nullptr;

    // Holds Freeable object associated by a pointer passed to sqlite as `user_data`
    struct : MutexGuarded {
        std::unordered_map<void*, Freeable*> map;
    } freeables { };

    struct {
        jclass illegalArgumentException;
    } java { };

    // KsqliteJni classes
    struct {
        struct : Class {
            jmethodID destroy; // ()V
        } destructorCallback;

        struct : Class {
            jmethodID resultCode; // ()I
            jmethodID message; // ()Ljava.lang.String;
        } jniException;
    } ksqlite { };

    // Handlers
    struct {
        Handler autoExtension;
        DestroyableHandler autoVacuumPages;
    } handlers { };
} KsqliteJniGlobalState;

#define K KsqliteJniGlobalState
#define KF K.freeables
#define KJV K.java
#define KKJE K.ksqlite.jniException
#define KKDC K.ksqlite.destructorCallback
#define KHAE K.handlers.autoExtension
#define KHAP K.handlers.autoVacuumPages

/**
 * Initializes and caches the Java related classes and objects.
 */
static void initializeJavaJniCache(JNIEnv* env) {
    // IllegalArgumentException
    KJV.illegalArgumentException = RequireClass("java/lang/IllegalArgumentException");
}

/**
 * Initializes and caches the Ksqlite related classes and objects.
 */
static void initializeKsqliteJniCache(JNIEnv* env) {
    // DestructorCallback
    KKDC.klass = RequireKsqliteClass(DESTRUCTOR_CALLBACK);
    KKDC.destroy = RequireKsqliteMethod(KKDC, "destroy", "()V", DESTRUCTOR_CALLBACK);

    // KsqliteJniException
    KKJE.klass = RequireKsqliteClass(KSQLITE_JNI_EXCEPTION);
    KKJE.resultCode = RequireKsqliteMethod(KKJE, "getResultCode", "()I", KSQLITE_JNI_EXCEPTION);

    KKJE.message =
        RequireKsqliteMethod(KKJE, "getMessage", "()Ljava/lang/String;", KSQLITE_JNI_EXCEPTION);
}

/**
 * Initializes and cache mutexes.
 */
static void initializeMutexes(JNIEnv* env) {
    MutexAllocate(KF);
    MutexAllocate(KHAE);
    MutexAllocate(KHAP);
}

///////////////////////////////////////////////////////////////////////////
// JNI Lifecycle
///////////////////////////////////////////////////////////////////////////

#define JNI_VERSION_1_8 0x00010008
#define JNI_VERSION JNI_VERSION_1_8

/**
 * Returns the current JNIEnv object or abort if it cannot find the object.
 */
static JNIEnv* retrieveJniEnv() {
    JNIEnv* env = nullptr;
    const auto result = K.jvm->GetEnv(reinterpret_cast<void**>(env), JNI_VERSION);

    if (result != JNI_OK) {
        if (result == JNI_EDETACHED) {
            fprintf(stderr, "Fatal error: thread is not attached.\n");
        } else {
            fprintf(stderr, "Fatal error: cannot get current JNIEnv.\n");
        }

        abort();
    }

    return env;
}

JNIEXPORT jint JNICALL
JNI_OnLoad(
    JavaVM* vm,
    void* reserved
) {
    K.jvm = vm;
    const auto env = retrieveJniEnv();

    // Cache
    initializeJavaJniCache(env);
    initializeKsqliteJniCache(env);

    // Mutexes
    sqlite3_initialize();
    initializeMutexes(env);
    sqlite3_shutdown();

    return JNI_VERSION;
}

JNIEXPORT void JNICALL
JNI_OnUnload(
    JavaVM* vm,
    void* reserved
) {
    const auto env = retrieveJniEnv();

    if (!KF.map.empty()) {
        fprintf(stderr, "Statements did not cleaned up correctly.\n");
    }

    GlobalRefDestroy(KKDC.klass);
    KKDC.destroy = nullptr;

    GlobalRefDestroy(KKJE.klass);
    KKJE.message = nullptr;
    KKJE.resultCode = nullptr;

    MutexDestroy(KF);
    MutexDestroy(KHAE);
    MutexDestroy(KHAP);

    K.jvm = nullptr;
}

///////////////////////////////////////////////////////////////////////////
// Casting
///////////////////////////////////////////////////////////////////////////

#define PtrToLong(P) reinterpret_cast<jlong>(P)
#define LongToPtr(L) reinterpret_cast<void*>(L)

#define LongCast(T, L) reinterpret_cast<T*>(L)
#define LongTo_s3(L) LongCast(sqlite3, (L))
#define LongTo_s3_backup(L) LongCast(sqlite3_backup, (L))
#define LongTo_s3_blob(L) LongCast(sqlite3_blob, (L))
#define LongTo_s3_context(L) LongCast(sqlite3_context, (L))
#define LongTo_s3_stmt(L) LongCast(sqlite3_stmt, (L))
#define LongTo_s3_value(L) LongCast(sqlite3_value, (L))

///////////////////////////////////////////////////////////////////////////
// Buffer helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the direct buffer address of the given buffer or raises an exception if the address
 * cannot be obtained.
 */
static inline void* bufferDirectAddress(
    JNIEnv* env,
    jobject buffer
) {
    if (buffer == nullptr) {
        return nullptr;
    }

    const auto address = env->GetDirectBufferAddress(buffer);

    if (address == nullptr) {
        // TODO ensure java NIO is supported
        FatalError("Failed to get direct buffer address");
    }

    return address;
}

#define BufferDirectAddress(buffer) bufferDirectAddress(env, buffer)

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_createByteBuffer(
    JNIEnv* env,
    jclass clazz,
    jlong pointer,
    jlong size
) {
    const auto address = LongToPtr(pointer);

    if (address == nullptr) {
        return nullptr;
    }

    return env->NewDirectByteBuffer(address, size);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferRead(
    JNIEnv* env,
    jclass clazz,
    jobject buffer,
    jint size,
    jlong sourceOffset,
    jint destinationOffset,
    jbyteArray destination
) {
    const auto sourceAddress = BufferDirectAddress(buffer);

    if (sourceAddress == nullptr) {
        return;
    }

    const auto destinationAddress =
        static_cast<jbyte*>(env->GetPrimitiveArrayCritical(destination, nullptr));

    if (destinationAddress == nullptr) {
        return;
    }

    memcpy(
        destinationAddress + destinationOffset,
        static_cast<jbyte*>(sourceAddress) + sourceOffset,
        size
    );

    env->ReleasePrimitiveArrayCritical(destination, destinationAddress, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferWrite(
    JNIEnv* env,
    jclass clazz,
    jobject buffer,
    jbyteArray source,
    jint size,
    jint sourceOffset,
    jlong destinationOffset
) {
    const auto destinationAddress = BufferDirectAddress(buffer);

    if (destinationAddress == nullptr) {
        return;
    }

    const auto sourceAddress =
        static_cast<jbyte*>(env->GetPrimitiveArrayCritical(source, nullptr));

    if (sourceAddress == nullptr) {
        return;
    }

    memcpy(
        static_cast<jbyte*>(destinationAddress) + destinationOffset,
        sourceAddress + sourceOffset,
        size
    );

    env->ReleasePrimitiveArrayCritical(source, sourceAddress, 0);
}

///////////////////////////////////////////////////////////////////////////
// ByteArray helpers
///////////////////////////////////////////////////////////////////////////

/*
 * Creates a new jByteArray of length size, copies buffer's contents into it, and returns that byte
 * array (NULL on OOM unless fail-fast alloc errors are enabled).
*/
static jbyteArray bufferToByteArray(
    JNIEnv* const env,
    const void* const buffer,
    int length
) {
    if (buffer == nullptr) {
        return nullptr;
    }

    jbyteArray byteArray = env->NewByteArray(length);
    OutOfMemoryCheck(byteArray != nullptr);
    env->SetByteArrayRegion(byteArray, 0, length, static_cast<const jbyte*>(buffer));

    return byteArray;
}

/**
 * Returns the elements from the given byteArray or null if byteArray is null.
 * The returned byteArray must be freed with sqlite3_free() when no longer required.
 */
static jbyte* byteArrayToBuffer(
    JNIEnv* const env,
    jbyteArray byteArray,
    int length
) {
    if (byteArray == nullptr) {
        return nullptr;
    }

    const auto buffer = static_cast<jbyte*>(sqlite3_malloc(length));
    OutOfMemoryCheck(buffer != nullptr);
    env->GetByteArrayRegion(byteArray, 0, length, buffer);

    return buffer;
}

#define BufferToByteArray(buffer, length) bufferToByteArray(env, buffer, length)
#define ByteArrayToBuffer(byteArray, length) byteArrayToBuffer(env, byteArray, length)

///////////////////////////////////////////////////////////////////////////
// String helpers
///////////////////////////////////////////////////////////////////////////

/*
 * Converts Java String (UTF-16) to UTF-8 C string using Android AOSP Unicode transcoder.
 * Output must be freed with sqlite3_free().
 *
 * Returns nullptr on error or null input.
 */
static char* jstringToUtf8(
    JNIEnv* env,
    jstring string,
    size_t* outLength
) {
    if (string == nullptr) {
        if (outLength != nullptr) {
            *outLength = 0;
        }

        return nullptr;
    }

    const auto chars = env->GetStringChars(string, nullptr);

    if (chars == nullptr) {
        return nullptr;
    }

    const auto length = env->GetStringLength(string);

    const auto utf8Length = utf16_to_utf8_length(
        reinterpret_cast<const char16_t*>(chars),
        length
    );

    if (utf8Length <= 0) {
        env->ReleaseStringChars(string, chars);
        return nullptr;
    }

    const auto utf8 = static_cast<char*>(sqlite3_malloc(utf8Length + 1));
    OutOfMemoryCheck(utf8);

    utf16_to_utf8(
        reinterpret_cast<const char16_t*>(chars),
        length,
        utf8,
        utf8Length + 1
    );

    env->ReleaseStringChars(string, chars);

    if (outLength != nullptr) {
        *outLength = utf8Length;
    }

    return utf8;
}

/*
 * Converts UTF-8 C string to Java String (UTF-16) using Android AOSP Unicode transcoder.
 */
static jstring utf8ToJstring(
    JNIEnv* env,
    const char* utf8,
    size_t length
) {
    if (utf8 == nullptr) {
        return nullptr;
    }

    if (length == -1) {
        // This is what is used by sqlite internally
        length = 0x3fffffff & static_cast<int>(strlen(utf8));
    }

    const auto utf16Length = utf8_to_utf16_length(
        reinterpret_cast<const uint8_t*>(utf8),
        length
    );

    if (utf16Length <= 0) {
        return env->NewString(nullptr, 0);
    }

    static const int jCharSize = sizeof(jchar);
    const auto utf16 = static_cast<jchar*>(sqlite3_malloc((utf16Length + 1) * jCharSize));

    if (utf16 == nullptr) {
        return nullptr;
    }

    utf8_to_utf16(
        reinterpret_cast<const uint8_t*>(utf8),
        length,
        reinterpret_cast<char16_t*>(utf16),
        utf16Length + 1
    );

    jstring string = env->NewString(utf16, utf16Length);
    sqlite3_free(utf16);

    return string;
}

#define JstringToUtf8Out(string, outLength) jstringToUtf8(env, string, outLength)
#define JstringToUtf8(string) JstringToUtf8Out(string, nullptr)
#define Utf8ToJstringLength(utf8, length) utf8ToJstring(env, utf8, length)
#define Utf8ToJstring(utf8) Utf8ToJstringLength(utf8, -1)

///////////////////////////////////////////////////////////////////////////
// Freeable allocation
///////////////////////////////////////////////////////////////////////////

/**
 * Insert a value into the freeable map.
 */
static void pushFreeable(
    JNIEnv* env,
    void* key,
    Freeable* value
) {
    MutexEnter(KF);
    auto [_, inserted] = KF.map.emplace(key, value);
    MutexLeave(KF);

    if (!inserted) {
        FatalError("An object already exists for the given key and may have not been cleaned up");
    }
}

/**
 * Remove and returns a value from the freeable map.
 */
static Freeable* popFreeable(
    JNIEnv* env,
    void* key
) {
    MutexEnter(KF);

    auto& map = KF.map;
    auto iterator = map.find(key);

    if (iterator == map.end()) {
        FatalError("No value exists for the given key, it may have been cleaned up previously"
                   " and reference tracking may be broken");
    }

    const auto value = iterator->second;

    map.erase(iterator);
    MutexLeave(KF);

    return value;
}

#define FreeablePush(key, value) pushFreeable(env, key, value)
#define FreeablePop(key) popFreeable(env, key)

///////////////////////////////////////////////////////////////////////////
// Parameter checks
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an IllegalArgumentException if the destructor argument is not null but argument is.
 */
#define DestructorCheck(argument, result) \
    if (argument == nullptr) { \
        env->ThrowNew(                    \
            KJV.illegalArgumentException, \
            "destructor must be null if " #argument " is null" \
        )         ;                        \
        return result; \
    }

///////////////////////////////////////////////////////////////////////////
// Callbacks
///////////////////////////////////////////////////////////////////////////

/**
 * Calls the Java destructor for the given Freeable and releases associated resources.
 * The pointer must have been allocated with `new`.
 */
static void destroyFreeable(
    JNIEnv* env,
    void* ptrToFreeable
) {
    const auto freeablePtr = reinterpret_cast<Freeable*>(RequireNonNull(ptrToFreeable));
    auto freeable = *freeablePtr;

    jobject destructor = LocalRefCreate(freeable.destructor);

    if (destructor != nullptr) {
        GlobalRefDestroy(freeable.destructor);
        env->CallVoidMethod(destructor, KKDC.destroy);
        LocalRefDestroy(destructor);
    }

    if (freeable.target != nullptr) {
        GlobalRefDestroy(freeable.target);
    }

    if (freeable.pointer != nullptr) {
        sqlite3_free(freeable.pointer);
    }

    delete freeablePtr;
}

/**
 * Calls the Java destructor for the given Freeable pointer and releases associated resources.
 * The pointer must have been allocated with `new`.
 */
static void freeableDestructor(void* ptrToFreeable) {
    const auto env = retrieveJniEnv();
    destroyFreeable(env, ptrToFreeable);
}

/**
 * Retrieves the Freeable pointer associated with pointer and calls the Java destructor releasing
 * associated resources.
 */
static void freeableDestructorPop(void* pointer) {
    const auto env = retrieveJniEnv();
    const auto ptrToFreeable = FreeablePop(pointer);
    destroyFreeable(env, ptrToFreeable);
}

/**
 * Pushes freeable and returns the destructor function for it.
 * Returns null if freeable is null.
 */
static inline DestructorFunction freeableDestructorPush(
    JNIEnv* env,
    void* key,
    Freeable* freeable
) {
    if (freeable == nullptr) {
        return nullptr;
    }

    FreeablePush(key, freeable);
    return freeableDestructorPop;
}

/**
 * Returns freeableDestructor() callback if given pointer P is not null.
 */
#define FreeableDestructor(P) (P) == nullptr ? nullptr : freeableDestructor
#define FreeableDestructorPush(K, F) freeableDestructorPush(env, K, F)

/**
 * Calls the Java destructor for the given handler and releases associated resources.
 */
static void handlerDestructor(void* ptrToHandler) {
    const auto env = retrieveJniEnv();
    const auto handlerPtr = reinterpret_cast<DestroyableHandler*>(ptrToHandler);
    auto handler = *handlerPtr;

    MutexEnter(handler);

    // Callback is optional
    if (handler.callback != nullptr) {
        GlobalRefDestroy(handler.callback);
        handler.callback = nullptr;
        handler.call = nullptr;
    }

    jobject destructor = nullptr;

    // Destructor is also optional but required for callback
    if (handler.destructor != nullptr) {
        destructor = LocalRefCreate(RequireNonNullJobject(handler.destructor));
        GlobalRefDestroy(handler.destructor);
        handler.destructor = nullptr;
    }

    MutexLeave(handler);

    if (destructor != nullptr) {
        env->CallVoidMethod(destructor, KKDC.destroy);
        LocalRefDestroy(destructor);
    }
}

/**
 * Returns handlerDestructor() callback if given pointer P is not null.
 */
#define HandlerDestructor(P) if ((P) == nullptr) nullptr else handlerDestructor

/**
 * Calls the Java AutoExtensionCallback.
 */
static int callAutoExtensionCallback(
    sqlite3* pDb,
    char** pzErr,
    const sqlite3_api_routines* pApi
) {
    const auto env = retrieveJniEnv();
    HandlerCallbackConsume(KHAE);

    const auto dbPtr = PtrToLong(pDb);
    const auto apiPtr = PtrToLong(pApi);
    auto rc = env->CallIntMethod(callback, call, dbPtr, apiPtr);
    LocalRefDestroy(callback);

    if (const auto exception = env->ExceptionOccurred(); exception != nullptr) {
        env->ExceptionClear();

        if (!env->IsInstanceOf(exception, KKJE.klass)) {
            FatalError("Unexpected exception type thrown in AutoExtensionCallback#call");
        }

        const auto message =
            reinterpret_cast<jstring>(env->CallObjectMethod(exception, KKJE.message));

        // Let Java handle theses unexpected method call exceptions
        IfExceptionThrown {
            rc = SQLITE_ERROR;
        } else {
            const auto utf8 = JstringToUtf8(message);

            if (utf8 != nullptr) {
                *pzErr = sqlite3_mprintf(utf8);
                sqlite3_free(message);
            }

            rc = env->CallIntMethod(exception, KKJE.resultCode);

            IfExceptionThrown {
                rc = SQLITE_ERROR;
            }
        }

        LocalRefDestroy(exception);
    }

    return rc;
}

/**
 * Calls the Java AutoVacuumPagesCallback.
 */
static unsigned int callAutoVacuumPagesCallback(
    void* pClientData,
    const char* zSchema,
    unsigned int nDbPage,
    unsigned int nFreePage,
    unsigned int nBytePerPage
) {
    const auto env = retrieveJniEnv();
    HandlerCallbackConsume(KHAP);

    const auto schema = Utf8ToJstring(zSchema);

    uint result = env->CallIntMethod(
        callback,
        call,
        schema,
        nDbPage,
        nFreePage,
        nBytePerPage
    );

    IfExceptionThrown {
        // Default behaviour
        result = nFreePage;
    }

    LocalRefDestroy(schema);
    LocalRefDestroy(callback);

    return result;
}

///////////////////////////////////////////////////////////////////////////
// SQLite 1 to 1 mapping
///////////////////////////////////////////////////////////////////////////

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1auto_1extension(
    JNIEnv* env,
    jclass clazz,
    jobject callback
) {
    if (callback == nullptr) {
        return SQLITE_MISUSE;
    }

    auto rc = SQLITE_OK;
    MutexEnter(KHAE);

    if (KHAE.callback != nullptr) {
        if (!env->IsSameObject(callback, KHAE.callback)) {
            rc = SQLITE_MISUSE;
        }
    } else {
        rc = ksqlite_auto_extension(callAutoExtensionCallback);

        if (rc == SQLITE_OK) {
            HandlerConfigure(KHAE, callback, "(JJ)I", AUTO_EXTENSION_CALLBACK);
        }
    }

    MutexLeave(KHAE);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1cancel_1auto_1extension(
    JNIEnv* env,
    jclass clazz,
    jobject callback
) {
    MutexEnter(KHAE);
    auto rc = 0;

    if (KHAE.callback != nullptr && env->IsSameObject(callback, KHAE.callback)) {
        GlobalRefDestroy(KHAE.callback);
        KHAE.call = nullptr;
        rc = ksqlite_cancel_auto_extension(callAutoExtensionCallback);
    }

    MutexLeave(KHAE);
    return rc;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1aggregate_1context(
    JNIEnv* env,
    jclass clazz,
    jlong context,
    jint nBytes
) {
    const auto s3Context = LongTo_s3_context(context);
    const auto pointer = sqlite3_aggregate_context(s3Context, nBytes);

    return PtrToLong(pointer);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1autovacuum_1pages(
    JNIEnv* env,
    jclass clazz,
    jlong db,
    jobject callback,
    jobject destructor
) {
    const auto pDb = LongTo_s3(db);

    // Force previous callback destructor invocation
    auto rc = sqlite3_autovacuum_pages(pDb, nullptr, nullptr, nullptr);

    if (rc != SQLITE_OK) {
        return rc;
    }

    MutexEnter(KHAP);

    // Ensure that destructor, if any, has been called
    RequireNull(KHAP.callback);
    RequireNull(KHAP.destructor);

    if (callback != nullptr) {
        rc = sqlite3_autovacuum_pages(pDb, callAutoVacuumPagesCallback, &KHAP, handlerDestructor);

        if (rc == SQLITE_OK) {
            HandlerWithDestructorConfigure(
                KHAP,
                destructor,
                callback,
                "(Ljava/lang/String;III)I",
                AUTO_VACUUM_PAGES_CALLBACK
            );
        }
    } else {
        // For now, forbid setting a destructor without a callback
        RequireNull(destructor);
    }

    MutexLeave(KHAP);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1finish(
    JNIEnv* env,
    jclass clazz,
    jlong backup
) {
    return sqlite3_backup_finish(LongTo_s3_backup(backup));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1init(
    JNIEnv* env,
    jclass clazz,
    jlong destDb,
    jstring destDbName,
    jlong srcDb,
    jstring srcDbName
) {
    const auto pDest = LongTo_s3(destDb);
    const auto pSource = LongTo_s3(srcDb);
    const auto zDestName = JstringToUtf8(destDbName);
    const auto zSourceName = JstringToUtf8(srcDbName);
    const auto backupPtr = sqlite3_backup_init(pDest, zDestName, pSource, zSourceName);

    sqlite3_free(zDestName);
    sqlite3_free(zSourceName);

    return PtrToLong(backupPtr);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1pagecount(
    JNIEnv* env,
    jclass clazz,
    jlong backup
) {
    return sqlite3_backup_pagecount(LongTo_s3_backup(backup));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1remaining(
    JNIEnv* env,
    jclass clazz,
    jlong backup
) {
    return sqlite3_backup_remaining(LongTo_s3_backup(backup));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1backup_1step(
    JNIEnv* env,
    jclass clazz,
    jlong backup,
    jint nPage
) {
    return sqlite3_backup_step(LongTo_s3_backup(backup), nPage);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1blob(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jbyteArray data,
    jint size,
    jobject destructor
) {
    DestructorCheck(data, SQLITE_MISUSE)

    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto buffer = ByteArrayToBuffer(data, size);
    const auto freeable = AllocateFreeablePointer(buffer, destructor);
    const auto pDestructor = FreeableDestructorPush(buffer, freeable);

    return sqlite3_bind_blob(pStmt, index, buffer, size, pDestructor);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1blob64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject data,
    jlong size,
    jobject destructor
) {
    DestructorCheck(data, SQLITE_MISUSE)

    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto buffer = BufferDirectAddress(data);
    const auto freeable = AllocateFreeableTarget(data, destructor);
    const auto pDestructor = FreeableDestructorPush(buffer, freeable);

    return sqlite3_bind_blob64(pStmt, index, buffer, size, pDestructor);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1double(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jdouble value
) {
    return sqlite3_bind_double(LongTo_s3_stmt(stmt), index, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1int(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jint value
) {
    return sqlite3_bind_int(LongTo_s3_stmt(stmt), index, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1int64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong value
) {
    return sqlite3_bind_int64(LongTo_s3_stmt(stmt), index, value);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1null(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return sqlite3_bind_null(LongTo_s3_stmt(stmt), index);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1parameter_1count(
    JNIEnv* env,
    jclass clazz,
    jlong stmt
) {
    return sqlite3_bind_parameter_count(LongTo_s3_stmt(stmt));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1parameter_1index(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jstring name
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto zName = JstringToUtf8(name);
    const auto index = sqlite3_bind_parameter_index(pStmt, zName);

    sqlite3_free(zName);
    return index;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1parameter_1name(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index
) {
    return Utf8ToJstring(sqlite3_bind_parameter_name(LongTo_s3_stmt(stmt), index));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1pointer(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject data,
    jstring type,
    jobject destructor
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto zType = JstringToUtf8(type);
    const auto freeable = AllocateFreeable(zType, data, destructor);
    const auto pDestructor = FreeableDestructor(freeable);

    return sqlite3_bind_pointer(pStmt, index, freeable, zType, pDestructor);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1text(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jstring text,
    jint size,
    jboolean computeSize
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    size_t textSize = 0;
    const auto buffer = JstringToUtf8Out(text, &textSize);
    const auto freeable = AllocateFreeablePointer(buffer, nullptr);
    const auto pDestructor = FreeableDestructorPush(buffer, freeable);

    if (computeSize == JNI_TRUE) {
        size = static_cast<jint>(textSize);
    }

    return sqlite3_bind_text(pStmt, index, buffer, size, pDestructor);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1text64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jobject data,
    jlong size,
    jobject destructor,
    jint encoding
) {
    const auto pStmt = LongTo_s3_stmt(stmt);
    const auto buffer = reinterpret_cast<char *>(BufferDirectAddress(data));
    const auto freeable = AllocateFreeableTarget(data, destructor);
    const auto pDestructor = FreeableDestructorPush(buffer, freeable);

    return sqlite3_bind_text64(pStmt, index, buffer, size, pDestructor, encoding);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1value(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong value
) {
    return sqlite3_bind_value(LongTo_s3_stmt(stmt), index, LongTo_s3_value(value));
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1zeroblob(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jint size
) {
    return sqlite3_bind_zeroblob(LongTo_s3_stmt(stmt), index, size);
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1zeroblob64(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jlong size
) {
    return sqlite3_bind_zeroblob64(LongTo_s3_stmt(stmt), index, size);
}