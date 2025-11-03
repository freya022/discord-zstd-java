#include "chunks.h"

size_t getTotalDataLength(const std::vector<const std::vector<jbyte> *> &chunks, const ZSTD_outBuffer &finalChunk) {
    size_t length = 0;
    length += finalChunk.pos;
    for (const auto &chunk: chunks) {
        length += chunk->size();
    }
    return length;
}

jbyteArray mergeChunks(
    JNIEnv *env, const std::vector<const std::vector<jbyte> *> &chunks, const ZSTD_outBuffer &finalChunk) {
    const size_t length = getTotalDataLength(chunks, finalChunk);

    jbyteArray finalOutput = env->NewByteArray(static_cast<jsize>(length));
    jsize offset = 0;
    for (const auto &chunk: chunks) {
        const auto chunkSize = static_cast<jsize>(chunk->size());
        env->SetByteArrayRegion(finalOutput, offset, chunkSize, chunk->data());
        offset += chunkSize;
    }
    env->SetByteArrayRegion(finalOutput, offset, static_cast<jsize>(finalChunk.pos),
                            static_cast<jbyte *>(finalChunk.dst));

    return finalOutput;
}
