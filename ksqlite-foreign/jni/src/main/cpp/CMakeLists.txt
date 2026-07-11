cmake_minimum_required(VERSION 3.22.1)
project("ksqlite-jni")

set(KSQLITE_JNI_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/ksqlite_jni_build")

add_library(${KSQLITE_LIB_NAME} SHARED
        ${KSQLITE_SOURCES}
        ksqlite-jni.cpp
        utils/Unicode.cpp
)

target_include_directories(${KSQLITE_LIB_NAME} PRIVATE ${KSQLITE_INCLUDES})
target_compile_definitions(${KSQLITE_LIB_NAME} PRIVATE ${KSQLITE_DEFINITIONS})
target_link_options(${KSQLITE_LIB_NAME} PRIVATE ${KSQLITE_LINKER_OPTIONS})
target_link_libraries(${KSQLITE_LIB_NAME} PRIVATE log)