#include <jni.h>
#include <string>
#include <sqlite3mc_amalgamation.h>

extern "C" JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteNative_ksqliteHello(
    JNIEnv* env,
    jclass clazz
) {
    std::string hello = "Hello from C++, Ksqlite";
    return env->NewStringUTF(hello.c_str());
}