#include <cstdlib>
#include <cstring>
#include <jni.h>

#include "ksqlite.h"
#include "utils/Unicode.h"

#ifndef KSQLITE_JNI
#define KSQLITE_JNI

// TODO move the following at the end of the when all the functions are defined
#endif //KSQLITE_JNI

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

#define RequireNull(O) requireNull(env, (O))
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
struct HandlerWithDestructor : Handler {
    jobject destructor;
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
    const auto callback = LocalRefCreate(RequireNonNull(handler.callback)); \
    const auto call = handler.call; \
    MutexLeave(handler)

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
    JavaVM* jvm;

    // KsqliteJni classes
    struct {
        struct : Class {
            jmethodID destroy; // ()V
        } destructorCallback;

        struct : Class {
            jmethodID resultCode; // ()I
            jmethodID message; // ()Ljava.lang.String;
        } jniException;
    } ksqlite;

    // Handlers
    struct {
        Handler autoExtension;
        HandlerWithDestructor autoVacuumPages;
    } handlers;
} KsqliteJniGlobalState;

#define KS KsqliteJniGlobalState
#define KKJE KS.ksqlite.jniException
#define KKDC KS.ksqlite.destructorCallback
#define KHAE KS.handlers.autoExtension
#define KHAP KS.handlers.autoVacuumPages

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
    const auto result = KS.jvm->GetEnv(reinterpret_cast<void**>(env), JNI_VERSION);

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
    KS.jvm = vm;
    const auto env = retrieveJniEnv();

    // Classes
    initializeKsqliteJniCache(env);

    // Sqlite3
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

    GlobalRefDestroy(KKDC.klass);
    KKDC.destroy = nullptr;

    GlobalRefDestroy(KKJE.klass);
    KKJE.message = nullptr;
    KKJE.resultCode = nullptr;

    MutexDestroy(KHAE);
    MutexDestroy(KHAP);

    KS.jvm = nullptr;
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

extern "C"
JNIEXPORT jobject JNICALL
Java_ksqlite_KsqliteJni_createByteBuffer(
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

    const auto address = static_cast<jbyte*>(baseAddress) + offset;
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

/*
 * Creates a new jByteArray of length size, copies buffer's contents into it, and returns that byte
 * array (NULL on OOM unless fail-fast alloc errors are enabled). buffer may be NULL, in which case
 * the array is created but no bytes are filled.
 *
 * From <sqlite3_jni.c>
*/
static jbyteArray createByteArray(
    JNIEnv* const env,
    const void* const buffer,
    int size
) {
    jbyteArray byteArray = env->NewByteArray(size);
    OutOfMemoryCheck(byteArray);

    if (byteArray != nullptr && buffer != nullptr) {
        env->SetByteArrayRegion(byteArray, 0, size, static_cast<const jbyte*>(buffer));
    }

    return byteArray;
}

#define CreateByteArray(buffer, size) createByteArray(env, buffer, size)

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

    const auto utf16 = static_cast<jchar*>(sqlite3_malloc((utf16Length + 1u) * sizeof(jchar)));

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
// Callbacks
///////////////////////////////////////////////////////////////////////////

/**
 * Calls the Java destructor for the given handler and releases associated resources.
 */
static void callDestructor(void* handlerPtr) {
    const auto env = retrieveJniEnv();
    auto handler = (*(HandlerWithDestructor*) handlerPtr);

    MutexEnter(handler);

    // Callback is optional
    if (handler.callback != nullptr) {
        GlobalRefDestroy(handler.callback);
        KHAE.call = nullptr;
    }

    jobject destructor = nullptr;

    // Destructor is also optional but required for callback
    if (handler.destructor != nullptr) {
        destructor = LocalRefCreate(RequireNonNull(handler.destructor));
        GlobalRefDestroy(handler.destructor);
    }

    MutexLeave(handler);

    if (destructor != nullptr) {
        env->CallVoidMethod(destructor, KKDC.destroy);
        LocalRefDestroy(destructor);
    }
}

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
            env->FatalError("Unexpected exception type thrown in AutoExtensionCallback#call");
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

    auto result = env->CallIntMethod(
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
    const auto s3 = LongTo_s3(db);

    // Force previous callback destructor invocation
    auto rc = sqlite3_autovacuum_pages(s3, nullptr, nullptr, nullptr);

    if (rc != SQLITE_OK) {
        return rc;
    }

    MutexEnter(KHAP);

    // Ensure that destructor, if any, has been called
    RequireNull(KHAP.callback);
    RequireNull(KHAP.destructor);

    if (callback != nullptr) {
        rc = sqlite3_autovacuum_pages(s3, callAutoVacuumPagesCallback, &KHAP, callDestructor);

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
    // TODO: implement sqlite3_bind_blob()
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
    // TODO: implement sqlite3_bind_blob64()
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
    // TODO: implement sqlite3_bind_pointer()
}

extern "C"
JNIEXPORT jint JNICALL
Java_ksqlite_KsqliteJni_sqlite3_1bind_1text(
    JNIEnv* env,
    jclass clazz,
    jlong stmt,
    jint index,
    jstring text,
    jint size
) {
    // TODO: implement sqlite3_bind_text()
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
    jint encoding,
    jobject destructor
) {
    // TODO: implement sqlite3_bind_text64()
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