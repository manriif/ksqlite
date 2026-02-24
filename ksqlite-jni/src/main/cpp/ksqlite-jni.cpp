#include <jni.h>
#include <ksqlite.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_ksqlite_KsqliteJni_ksqliteHello(
    JNIEnv* env,
    jclass clazz
) {
    std::string hello = "Hello from C++, Ksqlite";
    return env->NewStringUTF(hello.c_str());
}