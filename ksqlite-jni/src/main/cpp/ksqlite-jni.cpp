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

// Java
#define JAVA_STRING "java.lang.String"

// Ksqlite
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

    // Java classes
    struct {
        struct : Class {
            jmethodID constructor; // ([BLjava/nio/charset/Charset;)V
            jmethodID getBytes; // (Ljava/nio/charset/Charset;)[B
        } string;

        struct {
            jobject utf8; // Ljava/nio/charset/Charset;
        } charset;
    } java;

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
#define KJS KS.java.string
#define KJC KS.java.charset
#define KKJE KS.ksqlite.jniException
#define KKDC KS.ksqlite.destructorCallback
#define KHAE KS.handlers.autoExtension
#define KHAP KS.handlers.autoVacuumPages

/**
 * Initializes and caches the Java related classes and objects.
 */
static void initializeJavaClassCache(JNIEnv* env) {
    KJS.klass = RequireClass("java/lang/String");
    KJS.constructor = RequireMethod(KJS, "<init>", "([BLjava/nio/charset/Charset;)V", JAVA_STRING);
    KJS.getBytes = RequireMethod(KJS, "getBytes", "(Ljava/nio/charset/Charset;)[B", JAVA_STRING);

    const auto klass = env->FindClass("java/nio/charset/StandardCharsets");
    ExceptionClearAndAbort("Error getting reference to java.nio.charset.StandardCharsets class");

    const auto fieldId = env->GetStaticFieldID(klass, "UTF_8", "Ljava/nio/charset/Charset;");

    ExceptionClearAndAbort(
        "Error getting reference to java.nio.charset.StandardCharsets.UTF_8 field"
    );

    KJC.utf8 = GlobalRefCreate(env->GetStaticObjectField(klass, fieldId));

    ExceptionClearAndAbort(
        "Error getting reference to java.nio.charset.StandardCharsets.UTF_8 instance"
    );

    LocalRefDestroy(klass);
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
    initializeJavaClassCache(env);
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

    GlobalRefDestroy(KJS.klass);
    KJS.constructor = nullptr;

    GlobalRefCreate(KJC.utf8);

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
#define LongCast_s3(L) LongCast(sqlite3, (L))
#define LongCast_s3_backup(L) LongCast(sqlite3_backup, (L))
#define LongCast_s3_blob(L) LongCast(sqlite3_blob, (L))
#define LongCast_s3_context(L) LongCast(sqlite3_context, (L))
#define LongCast_s3_stmt(L) LongCast(sqlite3_stmt, (L))
#define LongCast_s3_value(L) LongCast(sqlite3_value, (L))

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

/**
 * Compute a string length that is limited to what can be stored in lower 30 bits of a 32-bit signed
 * integer.
 *
 * The value returned will never be negative.  Nor will it ever be greater than the actual length of
 * the string.  For very long strings (greater than 1GiB) the value returned might be less than the
 * true string length.
 *
 * From official <sqlite3.c>
 */
static int sqlite3Strlen30(const char* const z) {
    if (z == nullptr) {
        return 0;
    }

    return 0x3fffffff & (int) strlen(z);
}

/**
 * Uses the java.lang.String(byte[],Charset) constructor to create a new String from UTF-8 string
 * buffer. size is the number of bytes to copy. If size<0 then sqlite3Strlen30() is used to
 * calculate it.
 *
 * Returns NULL if buffer is NULL or on OOM, else returns a new jstring owned by the caller.
 *
 * Sidebar: this is a painfully inefficient way to convert from standard UTF-8 to a Java string, but
 * JNI offers only algorithms for working with MUTF-8, not UTF-8.
 *
 * From <sqlite3_jni.c>
 */
static jstring utf8ToJstring(
    JNIEnv* const env,
    const char* const buffer,
    int size
) {
    if (buffer == nullptr) {
        return nullptr;
    }

    jstring string = nullptr;

    if (0 == size || (size < 0 && !buffer[0])) {
        /* Fast-track the empty-string case via the MUTF-8 API. We could hypothetically do this for
         * any strings where size<4 and buffer is  NUL-terminated and none of buffer[0..3] are NUL
         * bytes. */
        string = env->NewStringUTF("");
        OutOfMemoryCheck(string != nullptr);
    } else {
        jbyteArray byteArray;

        if (size < 0) {
            size = sqlite3Strlen30(buffer);
        }

        byteArray = CreateByteArray(buffer, size);

        if (byteArray) {
            string = reinterpret_cast<jstring>(
                env->NewObject(KJS.klass, KJS.constructor, byteArray, KJC.utf8)
            );

            IfExceptionThrown {
                env->ExceptionDescribe();
                env->ExceptionClear();
            }

            LocalRefDestroy(byteArray);
        }

        OutOfMemoryCheck(string);
    }

    return string;
}

/**
static char * utf8ToBuffer() {

}

/**
 * Allocates a null terminated buffer to hold a string.
 *
 * Note that Java exception must be checked after the function return if length is greater than
 * byteArray actual length.
 */
static char* byteArrayToBuffer(
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
 * Allocates a null terminated buffer to hold a string and write the length to outLength.
 */
static char* byteArrayToBufferOut(
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

    return byteArrayToBuffer(env, byteArray, length);
}

#define BufferToUtf8(buffer, size) bufferToUtf8(env, buffer, size)
#define ByteArrayToBuffer(byteArray, length) byteArrayToBuffer(env, byteArray, lenght)
#define ByteArrayToBufferOut(byteArray, outLength) byteArrayToBufferOut(env, byteArray, outLength)

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
            const auto buffer = ByteArrayToBufferOut(message, nullptr);

            if (buffer != nullptr) {
                *pzErr = sqlite3_mprintf(buffer);
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

    const auto schema = BufferToUtf8(zSchema, -1);

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
    const auto s3Context = LongCast_s3_context(context);
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
    const auto s3 = LongCast_s3(db);

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