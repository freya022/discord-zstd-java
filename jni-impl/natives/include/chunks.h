#ifndef NATIVES_CHUNKS_H
#define NATIVES_CHUNKS_H

#include <jni.h>
#include <vector>
#include <zstd.h>

jbyteArray mergeChunks(
    JNIEnv *env, const std::vector<std::vector<jbyte> > &chunks, const ZSTD_outBuffer &finalChunk);

#endif
