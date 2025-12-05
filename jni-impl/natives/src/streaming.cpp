#include <memory>
#include <streaming.hpp>

State::State(ZSTD_DCtx *zds) {
    this->zds = zds;
    this->srcPos = 0;
}

jlong DiscordZstdJNIInputStream_newState(JNIEnv *, jclass, const jlong zdsPtr) {
    const auto zds = reinterpret_cast<ZSTD_DCtx *>(zdsPtr);
    return reinterpret_cast<jlong>(new State(zds));
}

void DiscordZstdJNIInputStream_freeState(JNIEnv *, jclass, const jlong ctxPtr) {
    const auto ctx = reinterpret_cast<State *>(ctxPtr);
    delete ctx;
}

jlong DiscordZstdJNIInputStream_inflate0(JNIEnv *env, jclass,
                                         const jlong statePtr,
                                         jbyteArray srcJ, const jlong srcSize,
                                         jbyteArray dstJ, const jlong dstOff, const jlong dstSize) {
    const auto state = reinterpret_cast<State *>(statePtr);

    const auto src = env->GetPrimitiveArrayCritical(srcJ, nullptr);
    const auto dst = env->GetPrimitiveArrayCritical(dstJ, nullptr);

    ZSTD_inBuffer inBuffer = {
        .src = src,
        .size = static_cast<size_t>(srcSize),
        .pos = state->srcPos,
    };

    ZSTD_outBuffer outBuffer = {
        .dst = static_cast<int8_t *>(dst) + dstOff,
        .size = static_cast<size_t>(dstSize),
        .pos = 0,
    };

    const auto previousInputOffset = inBuffer.pos;

    const auto result = ZSTD_decompressStream(state->zds, &outBuffer, &inBuffer);
    state->srcPos = inBuffer.pos;

    env->ReleasePrimitiveArrayCritical(srcJ, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(dstJ, dst, 0);

    const bool madeForwardProgress = inBuffer.pos > previousInputOffset || outBuffer.pos > 0;
    const bool fullyProcessedInput = inBuffer.pos == inBuffer.size;

    if (!madeForwardProgress && fullyProcessedInput) {
        return -1; // EOF
    }

    if (ZSTD_isError(result)) {
        const auto errorName = ZSTD_getErrorName(result);
        const auto exceptionClass = env->FindClass("dev/freya02/discord/zstd/api/DiscordZstdException");
        env->ThrowNew(exceptionClass, errorName);

        return -1;
    }

    return static_cast<jlong>(outBuffer.pos);
}
