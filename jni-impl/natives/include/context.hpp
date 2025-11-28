#ifndef LIBZSTD_JNI_CONTEXT_HPP
#define LIBZSTD_JNI_CONTEXT_HPP

#include <jni.h>

#define ZstdJNIContext_createDStream Java_dev_freya02_discord_zstd_jni_ZstdJNIContext_createDStream
#define ZstdJNIContext_freeDStream Java_dev_freya02_discord_zstd_jni_ZstdJNIContext_freeDStream
#define ZstdJNIContext_initDStream Java_dev_freya02_discord_zstd_jni_ZstdJNIContext_initDStream
#define ZstdJNIContext_decompress0 Java_dev_freya02_discord_zstd_jni_ZstdJNIContext_decompress0

extern "C" {
JNIEXPORT jlong JNICALL ZstdJNIContext_createDStream(JNIEnv *env, jclass obj);

JNIEXPORT void JNICALL ZstdJNIContext_freeDStream(JNIEnv *env, jclass obj, jlong zdsPtr);

JNIEXPORT jlong JNICALL ZstdJNIContext_initDStream(JNIEnv *env, jclass obj, jlong zdsPtr);
}

#endif //LIBZSTD_JNI_CONTEXT_HPP
