#include <memory>
#include <streaming.hpp>

Context::Context(ZSTD_DCtx *zds) {
    this->zds = zds;
    this->srcPos = 0;
}

jlong ZstdJNIInputStream_newContext(JNIEnv *, jclass, const jlong zdsPtr) {
    const auto zds = reinterpret_cast<ZSTD_DCtx *>(zdsPtr);
    return reinterpret_cast<jlong>(new Context(zds));
}

void ZstdJNIInputStream_freeContext(JNIEnv *, jclass, const jlong ctxPtr) {
    const auto ctx = reinterpret_cast<Context *>(ctxPtr);
    delete ctx;
}

jlong ZstdJNIInputStream_inflate0(JNIEnv *env, jclass,
                                  const jlong ctxPtr,
                                  jbyteArray srcJ, const jlong srcSize,
                                  jbyteArray dstJ, const jlong dstOff, const jlong dstSize) {
    const auto ctx = reinterpret_cast<Context *>(ctxPtr);

    const auto src = env->GetPrimitiveArrayCritical(srcJ, nullptr);
    const auto dst = env->GetPrimitiveArrayCritical(dstJ, nullptr);

    ZSTD_inBuffer inBuffer = {
        .src = src,
        .size = static_cast<size_t>(srcSize),
        .pos = ctx->srcPos,
    };

    ZSTD_outBuffer outBuffer = {
        .dst = static_cast<int8_t *>(dst) + dstOff,
        .size = static_cast<size_t>(dstSize),
        .pos = 0,
    };

    const auto previousInputOffset = inBuffer.pos;

    const auto result = ZSTD_decompressStream(ctx->zds, &outBuffer, &inBuffer);
    ctx->srcPos = inBuffer.pos;

    env->ReleasePrimitiveArrayCritical(srcJ, src, JNI_ABORT);
    env->ReleasePrimitiveArrayCritical(dstJ, dst, 0);

    const bool madeForwardProgress = inBuffer.pos > previousInputOffset || outBuffer.pos > 0;
    const bool fullyProcessedInput = inBuffer.pos == inBuffer.size;

    if (!madeForwardProgress && fullyProcessedInput) {
        return -1; // EOF
    }

    if (ZSTD_isError(result)) {
        const auto errorName = ZSTD_getErrorName(result);
        const auto exceptionClass = env->FindClass("dev/freya02/discord/zstd/api/ZstdException");
        env->ThrowNew(exceptionClass, errorName);

        return -1;
    }

    return static_cast<jlong>(outBuffer.pos);
}
