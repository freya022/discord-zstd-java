#ifndef LIBZSTD_JNI_CONTEXT_HPP
#define LIBZSTD_JNI_CONTEXT_HPP

#include <jni.h>

#define DiscordZstdJNIContext_createDStream Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIContext_createDStream
#define DiscordZstdJNIContext_freeDStream Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIContext_freeDStream
#define DiscordZstdJNIContext_initDStream Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIContext_initDStream
#define DiscordZstdJNIContext_decompress0 Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIContext_decompress0

extern "C" {
JNIEXPORT jlong JNICALL DiscordZstdJNIContext_createDStream(JNIEnv *env, jclass obj);

JNIEXPORT void JNICALL DiscordZstdJNIContext_freeDStream(JNIEnv *env, jclass obj, jlong zdsPtr);

JNIEXPORT jlong JNICALL DiscordZstdJNIContext_initDStream(JNIEnv *env, jclass obj, jlong zdsPtr);
}

#endif //LIBZSTD_JNI_CONTEXT_HPP
