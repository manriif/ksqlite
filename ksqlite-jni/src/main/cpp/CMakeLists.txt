cmake_minimum_required(VERSION 3.22.1)
project("ksqlite-jni")

set(KSQLITE_JNI_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/ksqlite_jni_build")
add_subdirectory(${SQLITE_CMAKE_DIR} ${KSQLITE_JNI_BUILD_DIR})

add_library(${KSQLITE_LIB_NAME} SHARED
        ksqlite-jni.cpp
        utils/Unicode.cpp
)

target_link_libraries(${KSQLITE_LIB_NAME} PRIVATE
        "-Wl,--whole-archive"
        ${SQLITE_LIB_NAME}
        "-Wl,--no-whole-archive"
)

# TODO: theses sould be removed and generated from Gradle after the JNI implementation is done
target_compile_definitions(${KSQLITE_LIB_NAME} PRIVATE
        SQLITE_ENABLE_PREUPDATE_HOOK=1
)