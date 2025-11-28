cmake_minimum_required(VERSION 3.25)
project(libzstd-jni CXX)

set(CMAKE_CXX_STANDARD 20)

add_library(libzstd-jni SHARED src/decompressor.cpp src/chunks.cpp
        src/streaming.cpp
        src/context.cpp)

set_target_properties(
        libzstd-jni
        PROPERTIES
        LIBRARY_OUTPUT_NAME zstd-jni)

include_directories(include)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

include(FetchContent)

set(ZSTD_BUILD_STATIC ON)
set(ZSTD_BUILD_SHARED OFF)
set(ZSTD_BUILD_PROGRAMS OFF)
set(ZSTD_LEGACY_SUPPORT OFF)
set(ZSTD_BUILD_DICTBUILDER OFF)
set(ZSTD_BUILD_COMPRESSION OFF)

FetchContent_Declare(
        zstd
        URL https://github.com/facebook/zstd/releases/download/v1.5.7/zstd-1.5.7.tar.gz
        DOWNLOAD_EXTRACT_TIMESTAMP TRUE
        SOURCE_SUBDIR build/cmake
)

FetchContent_MakeAvailable(zstd)

target_link_libraries(
        ${PROJECT_NAME}
        PRIVATE
        libzstd_static
)

# On windows and macos this is needed
target_include_directories(
        ${PROJECT_NAME}
        PRIVATE
        ${zstd_SOURCE_DIR}/lib
)
