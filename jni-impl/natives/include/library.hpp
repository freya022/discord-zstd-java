#ifndef NATIVES_LIBRARY_H
#define NATIVES_LIBRARY_H

#include <jni.h>

extern "C" {
JNIEXPORT jlong JNICALL Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_createDStream(
    JNIEnv *env, jclass obj);

JNIEXPORT jlong JNICALL Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_freeDStream(
    JNIEnv *env, jclass obj, jlong zds);

JNIEXPORT jint JNICALL Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_DStreamOutSize(
    JNIEnv *env, jclass obj);

JNIEXPORT jlong JNICALL Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_initDStream(
    JNIEnv *env, jclass obj, jlong zds);

JNIEXPORT jbyteArray JNICALL Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_decompressMessage(
    JNIEnv *env, jclass obj, jlong zds, jbyteArray bufferArray, jbyteArray inputArray);
}

#endif // NATIVES_LIBRARY_H
