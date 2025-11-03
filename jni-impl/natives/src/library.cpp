#include <chunks.h>
#include <library.h>
#include <vector>
#include <zstd.h>

jlong Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_createDStream(
    JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(ZSTD_createDStream());
}

jlong Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_freeDStream(
    JNIEnv *, jclass, jlong zds) {
    return ZSTD_freeDStream(reinterpret_cast<ZSTD_DStream *>(zds));
}

jint Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_DStreamOutSize(
    JNIEnv *, jclass) {
    return ZSTD_DStreamOutSize();
}

jlong Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_initDStream(
    JNIEnv *, jclass, jlong zds) {
    return ZSTD_initDStream(reinterpret_cast<ZSTD_DStream *>(zds));
}

jbyteArray Java_dev_freya02_discord_zstd_jni_ZstdJNIDecompressor_decompressMessage(
    JNIEnv *env, jclass, jlong zds, jbyteArray bufferArray, jbyteArray inputArray) {
    ZSTD_outBuffer output;
    output.dst = env->GetPrimitiveArrayCritical(bufferArray, nullptr);
    output.size = env->GetArrayLength(bufferArray);
    output.pos = 0;

    ZSTD_inBuffer input;
    input.src = env->GetPrimitiveArrayCritical(inputArray, nullptr);
    input.size = env->GetArrayLength(inputArray);
    input.pos = 0;

    std::vector<std::vector<jbyte>> chunks;

    while (true) {
        // In cases where the output buffer is too small for the decompressed input,
        // we'll loop back, so, reset the output position
        output.pos = 0;

        const std::size_t previousInputOffset = input.pos;

        const std::size_t result = ZSTD_decompressStream(reinterpret_cast<ZSTD_DStream *>(zds), &output, &input);

        const bool madeForwardProgress = input.pos > previousInputOffset || output.pos > 0;
        const bool fullyProcessedInput = input.pos == input.size;

        // Only merge when no input was consumed,
        // Zstd may have decompressed data in its buffers that it will hand off to us without consuming input
        if (result == 0 || (!madeForwardProgress && fullyProcessedInput)) {
            const auto decompressed = mergeChunks(env, chunks, output);

            env->ReleasePrimitiveArrayCritical(bufferArray, output.dst, 0);
            env->ReleasePrimitiveArrayCritical(inputArray, const_cast<void *>(input.src), 0);
            return decompressed;
        } else if (ZSTD_isError(result)) {
            env->ReleasePrimitiveArrayCritical(bufferArray, output.dst, 0);
            env->ReleasePrimitiveArrayCritical(inputArray, const_cast<void *>(input.src), 0);

            const auto errorName = ZSTD_getErrorName(result);
            const auto exceptionClass = env->FindClass("dev/freya02/discord/zstd/api/ZstdException");
            env->ThrowNew(exceptionClass, errorName);

            return nullptr;
        } else {
            // Copy our output
            const auto outputBytes = static_cast<jbyte *>(output.dst);
            // Same as:
            // auto chunk = std::vector(outputBytes, outputBytes + output.pos)
            // chunks.push_back(std::move(chunk))
            // Also same as:
            // chunks.push_back(std::vector(outputBytes, outputBytes + output.pos))
            chunks.emplace_back(outputBytes, outputBytes + output.pos);
        }
    }
}
