#ifndef NATIVES_STREAMING_HPP
#define NATIVES_STREAMING_HPP

#include <jni.h>
#include <zstd.h>

#define ZstdJNIInputStream_newContext Java_dev_freya02_discord_zstd_jni_ZstdJNIInputStream_newContext
#define ZstdJNIInputStream_freeContext Java_dev_freya02_discord_zstd_jni_ZstdJNIInputStream_freeContext
#define ZstdJNIInputStream_inflate0 Java_dev_freya02_discord_zstd_jni_ZstdJNIInputStream_inflate0

class Context {
public:
    ZSTD_DCtx *zds;
    size_t srcPos;

    explicit Context(ZSTD_DCtx *zds);
};

extern "C" {
JNIEXPORT jlong JNICALL ZstdJNIInputStream_newContext(JNIEnv *env, jclass obj, jlong zdsPtr);

JNIEXPORT void JNICALL ZstdJNIInputStream_freeContext(JNIEnv *env, jclass obj, jlong ctxPtr);

JNIEXPORT jlong JNICALL ZstdJNIInputStream_inflate0(JNIEnv *env, jclass,
                                                    jlong ctxPtr,
                                                    jbyteArray srcJ, jlong srcSize,
                                                    jbyteArray dstJ, jlong dstOff, jlong dstSize);
}

#endif // NATIVES_STREAMING_HPP
