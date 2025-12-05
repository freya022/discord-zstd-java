#ifndef NATIVES_DECOMPRESSOR_H
#define NATIVES_DECOMPRESSOR_H

#include <jni.h>

#define DiscordZstdJNIDecompressor_createDStream Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIDecompressor_createDStream
#define DiscordZstdJNIDecompressor_freeDStream Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIDecompressor_freeDStream
#define DiscordZstdJNIDecompressor_DStreamOutSize Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIDecompressor_DStreamOutSize
#define DiscordZstdJNIDecompressor_initDStream Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIDecompressor_initDStream
#define DiscordZstdJNIDecompressor_decompressMessage Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIDecompressor_decompressMessage

extern "C" {
JNIEXPORT jlong JNICALL DiscordZstdJNIDecompressor_createDStream(JNIEnv *env, jclass obj);

JNIEXPORT jlong JNICALL DiscordZstdJNIDecompressor_freeDStream(JNIEnv *env, jclass obj, jlong zds);

JNIEXPORT jint JNICALL DiscordZstdJNIDecompressor_DStreamOutSize(JNIEnv *env, jclass obj);

JNIEXPORT jlong JNICALL DiscordZstdJNIDecompressor_initDStream(JNIEnv *env, jclass obj, jlong zds);

JNIEXPORT jbyteArray JNICALL DiscordZstdJNIDecompressor_decompressMessage(JNIEnv *env, jclass obj,
                                                                          jlong zds,
                                                                          jbyteArray bufferArray,
                                                                          jbyteArray inputArray);
}

#endif // NATIVES_DECOMPRESSOR_H
