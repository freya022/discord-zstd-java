#include <context.hpp>
#include <zstd.h>

jlong ZstdJNIContext_createDStream(JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(ZSTD_createDStream());
}

void ZstdJNIContext_freeDStream(JNIEnv *, jclass, const jlong zdsPtr) {
    const auto zds = reinterpret_cast<ZSTD_DCtx *>(zdsPtr);
    ZSTD_freeDStream(zds);
}

jlong ZstdJNIContext_initDStream(JNIEnv *, jclass, const jlong zdsPtr) {
    const auto zds = reinterpret_cast<ZSTD_DCtx *>(zdsPtr);
    return static_cast<jlong>(ZSTD_initDStream(zds));
}
