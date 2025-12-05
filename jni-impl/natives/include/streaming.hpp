#ifndef NATIVES_STREAMING_HPP
#define NATIVES_STREAMING_HPP

#include <jni.h>
#include <zstd.h>

#define DiscordZstdJNIInputStream_newState Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIInputStream_newState
#define DiscordZstdJNIInputStream_freeState Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIInputStream_freeState
#define DiscordZstdJNIInputStream_inflate0 Java_dev_freya02_discord_zstd_jni_DiscordZstdJNIInputStream_inflate0

class State {
public:
    ZSTD_DCtx *zds;
    size_t srcPos;

    explicit State(ZSTD_DCtx *zds);
};

extern "C" {
JNIEXPORT jlong JNICALL DiscordZstdJNIInputStream_newState(JNIEnv *env, jclass obj, jlong zdsPtr);

JNIEXPORT void JNICALL DiscordZstdJNIInputStream_freeState(JNIEnv *env, jclass obj, jlong ctxPtr);

JNIEXPORT jlong JNICALL DiscordZstdJNIInputStream_inflate0(JNIEnv *env, jclass,
                                                           jlong statePtr,
                                                           jbyteArray srcJ, jlong srcSize,
                                                           jbyteArray dstJ, jlong dstOff, jlong dstSize);
}

#endif // NATIVES_STREAMING_HPP
