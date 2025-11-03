#ifndef NATIVES_CHUNKS_H
#define NATIVES_CHUNKS_H

#include <jni.h>
#include <vector>
#include <zstd.h>

using Chunks = std::vector<std::vector<jbyte> >;

jbyteArray mergeChunks(
    JNIEnv *env, const Chunks &chunks, const ZSTD_outBuffer &finalChunk);

#endif
