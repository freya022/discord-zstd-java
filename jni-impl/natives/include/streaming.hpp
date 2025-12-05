#ifndef NATIVES_STREAMING_HPP
#define NATIVES_STREAMING_HPP

#include <jni.h>
#include <zstd.h>

#define DiscordZstdJNIInputStream_newContext Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIInputStream_newContext
#define DiscordZstdJNIInputStream_freeContext Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIInputStream_freeContext
#define DiscordZstdJNIInputStream_inflate0 Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIInputStream_inflate0

class Context {
public:
    ZSTD_DCtx *zds;
    size_t srcPos;

    explicit Context(ZSTD_DCtx *zds);
};

extern "C" {
JNIEXPORT jlong JNICALL DiscordZstdJNIInputStream_newContext(JNIEnv *env, jclass obj, jlong zdsPtr);

JNIEXPORT void JNICALL DiscordZstdJNIInputStream_freeContext(JNIEnv *env, jclass obj, jlong ctxPtr);

JNIEXPORT jlong JNICALL DiscordZstdJNIInputStream_inflate0(JNIEnv *env, jclass,
                                                           jlong ctxPtr,
                                                           jbyteArray srcJ, jlong srcSize,
                                                           jbyteArray dstJ, jlong dstOff, jlong dstSize);
}

#endif // NATIVES_STREAMING_HPP
