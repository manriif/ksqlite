#include <cstdlib>
#include <cstring>
#include <jni.h>

#include "ksqlite.h"

#ifndef KSQLITE_JNI
#define KSQLITE_JNI

// TODO move the following at the end of the when all the functions are defined
#endif //KSQLITE_JNI

///////////////////////////////////////////////////////////////////////////
// Classes
///////////////////////////////////////////////////////////////////////////

#define KSQLITE_JNI_EXCEPTION "KsqliteJniException"
#define AUTO_EXTENSION_CALLBACK "AutoExtensionCallback"

///////////////////////////////////////////////////////////////////////////
// Exceptions
///////////////////////////////////////////////////////////////////////////

/**
 * Raises a fatal error if object is not null.
 */
static inline void requireNull(
    JNIEnv* const env,
    jobject object
) {
    if (object != nullptr) {
        env->FatalError("Expected Java object to be null");
    }
}

/**
 * Raises a fatal error if object is null.
 */
static inline jobject requireNonNull(
    JNIEnv* const env,
    jobject object
) {
    if (object == nullptr) {
        env->FatalError("Expected Java object not to be null");
    }

    return object;
}

#define RequireNull(O) requireNonNull(env, (O))
#define RequireNonNull(O) requireNonNull(env, (O))

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
        env->FatalError(errorMessage);
    }
}

#define ExceptionClearAndAbort(M) exceptionClearAndAbort(env, (M))

/**
 * Raises a fatal error when an SQLite operation failed due to a lack of memory.
 */
static inline void raiseOutOfMemory(JNIEnv* const env) {
    env->FatalError("KSQLite JNI is out of memory.");
}

/**
 * Raises a fatal error if expression E is false.
 */
#define OutOfMemoryCheck(E) if (!(E)) raiseOutOfMemory(env)

static inline jclass classOrThrow(
    JNIEnv* const env,
    const char* name,
    const char* errorMessage
) {
    const auto klass = env->FindClass(name);
    ExceptionClearAndAbort(errorMessage);
    return klass;
}

#define RequireClass(klassName) classOrThrow(env, klassName, "Class " klassName " was not found")
#define RequireKsqliteClass(klassName) RequireClass("ksqlite." klassName)

/**
 * Raises a fatal error when a method was not found on a given class.
 */
static inline jmethodID methodIdOrThrow(
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

#define RequireMethod(klass, name, signature, className) \
    methodIdOrThrow(env, klass, name, signature,  "Method " className "#" name "() was not found")

#define RequireKsqliteMethod(klass, name, signature, className) \
    RequireMethod(klass, name, signature, "ksqlite." className)

/**
 * Raises a fatal error when a field was not found on a given class.
 */
static inline jfieldID fieldIdOrThrow(
    JNIEnv* const env,
    jclass klass,
    const char* name,
    const char* signature,
    const char* errorMessage
) {
    const auto fieldId = env->GetFieldID(klass, name, signature);
    ExceptionClearAndAbort(errorMessage);
    return fieldId;
}

#define RequireField(klass, name, signature, className) \
    fieldIdOrThrow(env, klass, name, signature,  "Field " className "#" name " was not found")

#define RequireKsqliteField(klass, name, signature, className) \
    RequireField(klass, name, signature, "ksqlite." className)

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
// Casting
///////////////////////////////////////////////////////////////////////////

#define PtrToLong(P) (jlong)((intptr_t)(P))
#define LongToPtr(L) (void*)((intptr_t)(L))

#define LongCast(T, L) (T*)((intptr_t)((L)))
#define LongCast_s3(L) LongCast(sqlite3, (L))
#define LongCast_s3_backup(L) LongCast(sqlite3_backup, (L))
#define LongCast_s3_blob(L) LongCast(sqlite3_blob, (L))
#define LongCast_s3_context(L) LongCast(sqlite3_context, (L))
#define LongCast_s3_stmt(L) LongCast(sqlite3_stmt, (L))
#define LongCast_s3_value(L) LongCast(sqlite3_value, (L))

///////////////////////////////////////////////////////////////////////////
// Mutex
///////////////////////////////////////////////////////////////////////////

#define MutexAllocate(S) \
    S.mutex = sqlite3_mutex_alloc(SQLITE_MUTEX_FAST); \
    OutOfMemoryCheck(S.mutex)

#define MutexEnter(S) sqlite3_mutex_enter(S.mutex)
#define MutexLeave(S) sqlite3_mutex_leave(S.mutex)

///////////////////////////////////////////////////////////////////////////
// Handler
///////////////////////////////////////////////////////////////////////////

/**
 * Common type for callback handlers.
 */
struct Handler {
    sqlite3_mutex* mutex;
    jobject callback;
    jmethodID call;
};

/**
 * Common type for callback handlers with a destructor.
 */
struct HandlerWithDestructor : Handler {
    jobject destructor;
    jmethodID destroy;
};

/**
 * Declares the common variables for handler callback invocation.
 * The callback is wrapped into a local reference that can be released when no longer required.
 */
#define HandlerCallbackDeclare(Handler) \
    MutexEnter(Handler); \
    const auto callback = LocalRefCreate(RequireNonNull(Handler.callback)); \
    const auto call = Handler.call; \
    MutexLeave(Handler)

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/*
** Global state, e.g. caches and metrics.
*/
struct KsqliteJniGlobal {
    JavaVM* jvm;

    // KsqliteJniException
    struct {
        jclass klass;
        jmethodID resultCode;
        jmethodID messageUtf8;
    } ksqliteJniException;

    // Handlers
    Handler autoExtension;
    HandlerWithDestructor autoVacuumPages;
};

static KsqliteJniGlobal KsqliteJniGlobalState;

#define KJG KsqliteJniGlobalState
#define KJE KJG.ksqliteJniException
#define KAE KJG.autoExtension
#define KAP KJG.autoVacuumPages

/**
 * Initializes and cache KsqliteJniException class and methods.
 */
static void initializeKsqliteJniException(JNIEnv* env) {
    KJE.klass = RequireKsqliteClass(KSQLITE_JNI_EXCEPTION);
    KJE.resultCode = RequireKsqliteMethod(KJE.klass, "getResultCode", "()I", KSQLITE_JNI_EXCEPTION);
    KJE.messageUtf8 =
        RequireKsqliteMethod(KJE.klass, "getMessageUtf8", "[B", KSQLITE_JNI_EXCEPTION);
}

/**
 * Initializes and cache mutexes.
 */
static void initializeMutexes(JNIEnv* env) {
    MutexAllocate(KAE);
    MutexAllocate(KAP);
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
    const auto result = KJG.jvm->GetEnv(reinterpret_cast<void**>(env), JNI_VERSION);

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
JNI_OnLoad(JavaVM* vm, void* reserved) {
    KJG.jvm = vm;
    const auto env = retrieveJniEnv();

    // Global
    initializeKsqliteJniException(env);

    // Sqlite3
    sqlite3_initialize();
    initializeMutexes(env);
    sqlite3_shutdown();

    return JNI_VERSION;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM* vm, void* reserved) {
    sqlite3_mutex_free(KJG.autoExtension.mutex);
    KJG.jvm = nullptr;
}

///////////////////////////////////////////////////////////////////////////
// String helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates a null terminated buffer to hold a string given its length.
 *
 * Note that Java exception must be checked after the function return if length is greater than
 * byteArray actual length.
 */
static char* allocateStringLength(
    JNIEnv* const env,
    jbyteArray byteArray,
    jsize length
) {
    if (byteArray == nullptr) {
        return nullptr;
    }

    auto bytes = (char*) sqlite3_malloc(length);

    if (bytes != nullptr) {
        env->GetByteArrayRegion(byteArray, 0, length, (jbyte*) bytes);

        IfExceptionNotThrown {
            bytes[length] = 0;
        }
    }

    return bytes;
}

/**
 * Allocates a null terminated buffer to hold a string and sets the length to outLength.
 */
static char* allocateString(
    JNIEnv* const env,
    jbyteArray byteArray,
    jsize* outLength
) {
    if (byteArray != nullptr) {
        return nullptr;
    }

    const auto length = env->GetArrayLength(byteArray);

    if (outLength != nullptr) {
        *outLength = length;
    }

    return allocateStringLength(env, byteArray, length);
}

#define AllocateStringLength(byteArray, length) allocateStringLength(env, byteArray, lenght)
#define AllocateString(byteArray, outLength) allocateString(env, byteArray, outLength)

///////////////////////////////////////////////////////////////////////////
// Buffer helpers
///////////////////////////////////////////////////////////////////////////

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_createBuffer(
    JNIEnv* env,
    jclass clazz,
    jlong pointer,
    jlong size,
    jlong offset
) {
    const auto baseAddress = LongToPtr(pointer);

    if (baseAddress == nullptr) {
        return nullptr;
    }

    const auto address = ((jbyte*) baseAddress) + offset;
    return env->NewDirectByteBuffer(address, size);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferRead(
    JNIEnv* env,
    jclass clazz,
    jlong pointer,
    jint size,
    jlong sourceOffset,
    jint destinationOffset,
    jbyteArray destination
) {
    const auto sourceAddress = LongToPtr(pointer);

    if (sourceAddress == nullptr) {
        return;
    }

    const auto destinationAddress = (jbyte*) env->GetPrimitiveArrayCritical(destination, nullptr);

    if (destinationAddress == nullptr) {
        return;
    }

    memcpy(
        destinationAddress + destinationOffset,
        ((jbyte*) sourceAddress) + sourceOffset,
        size
    );

    env->ReleasePrimitiveArrayCritical(destination, destinationAddress, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_ksqlite_KsqliteJni_nativeBufferWrite(
    JNIEnv* env,
    jclass clazz,
    jlong pointer,
    jbyteArray source,
    jint size,
    jint sourceOffset,
    jlong destinationOffset
) {
    const auto destinationAddress = LongToPtr(pointer);

    if (destinationAddress == nullptr) {
        return;
    }

    const auto sourceAddress = (jbyte*) env->GetPrimitiveArrayCritical(source, nullptr);

    if (sourceAddress == nullptr) {
        return;
    }

    memcpy(
        ((jbyte*) destinationAddress) + destinationOffset,
        sourceAddress + sourceOffset,
        size
    );

    env->ReleasePrimitiveArrayCritical(source, sourceAddress, 0);
}

///////////////////////////////////////////////////////////////////////////
// SQLite 1 to 1 mapping
///////////////////////////////////////////////////////////////////////////

static int autoExtensionHandler(
    sqlite3* pDb,
    char** pzErr,
    const sqlite3_api_routines* pApi
) {
    const auto env = retrieveJniEnv();
    HandlerCallbackDeclare(KAE);

    const auto dbPtr = PtrToLong(pDb);
    const auto apiPtr = PtrToLong(pApi);
    auto rc = env->CallIntMethod(callback, call, dbPtr, apiPtr);
    LocalRefDestroy(callback);

    if (const auto exception = env->ExceptionOccurred(); exception != nullptr) {
        env->ExceptionClear();

        if (!env->IsInstanceOf(exception, KJE.klass)) {
            env->FatalError("Unexpected exception type thrown in AutoExtensionCallback#call");
        }

        const auto messageUtf8 = (jbyteArray) env->CallObjectMethod(exception, KJE.messageUtf8);

        // Let Java handle theses unexpected method call exceptions
        IfExceptionThrown {
            rc = SQLITE_ERROR;
        } else {
            const auto message = AllocateString(messageUtf8, nullptr);

            if (message != nullptr) {
                *pzErr = sqlite3_mprintf(message);
                sqlite3_free(message);
            }

            rc = env->CallIntMethod(exception, KJE.resultCode);

            IfExceptionThrown {
                rc = SQLITE_ERROR;
            }
        }

        LocalRefDestroy(exception);
    }

    return rc;
}

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

    MutexEnter(KAE);

    if (KAE.callback != nullptr) {
        if (!env->IsSameObject(callback, KAE.callback)) {
            rc = SQLITE_MISUSE;
        }
    } else {
        rc = ksqlite_auto_extension(autoExtensionHandler);

        if (rc == SQLITE_OK) {
            const auto klass = env->GetObjectClass(callback);
            KAE.call = RequireKsqliteMethod(klass, "call", "(JJ)I", AUTO_EXTENSION_CALLBACK);
            KAE.callback = GlobalRefCreate(callback);
            LocalRefDestroy(klass);
        }
    }

    MutexLeave(KAE);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_ksqlite_1cancel_1auto_1extension(
    JNIEnv* env,
    jclass clazz,
    jobject callback
) {
    MutexEnter(KAE);
    auto rc = 0;

    if (KAE.callback != nullptr && env->IsSameObject(callback, KAE.callback)) {
        GlobalRefDestroy(KAE.callback);
        KAE.call = nullptr;
        rc = 1;
    }

    MutexLeave(KAE);
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
    const auto s3Context = LongCast_s3_context(context);
    const auto pointer = sqlite3_aggregate_context(s3Context, nBytes);

    return PtrToLong(pointer);
}

static unsigned int autoVacuumPagesHandler(
    void* pClientData,
    const char* zSchema,
    unsigned int nDbPage,
    unsigned int nFreePage,
    unsigned int nBytePerPage
) {
    const auto env = retrieveJniEnv();
    HandlerCallbackDeclare(KAP);
}

static void autoVacuumPagesDestructor(void*) {
    const auto env = retrieveJniEnv();
    MutexEnter(KAP);

    if (KAP.callback != nullptr) {
        GlobalRefDestroy(KAP.callback);
        KAE.call = nullptr;
    }

    GlobalRefDestroy(KAP.destructor);
    KAP.destroy = nullptr;

    MutexLeave(KAP);
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
    const auto s3 = LongCast_s3(db);

    // Force previous callback destruction
    auto rc = sqlite3_autovacuum_pages(s3, nullptr, nullptr, nullptr);

    MutexEnter(KAP);

    // Ensure destructor have been called
    RequireNull(KAP.callback);
    RequireNull(KAP.destructor);

    if (callback != nullptr) {
        rc = sqlite3_autovacuum_pages(autoExtensionHandler);

        if (rc == SQLITE_OK) {
            const auto klass = env->GetObjectClass(callback);
            KAE.call = RequireKsqliteMethod(klass, "call", "(JJ)I", AUTO_EXTENSION_CALLBACK);
            KAE.callback = GlobalRefCreate(callback);
            LocalRefDestroy(klass);
        }
    }

    MutexLeave(KAP);

    return rc;
}