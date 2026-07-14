#
# Copyright (C) 2026 Maanrifa Bacar Ali
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
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