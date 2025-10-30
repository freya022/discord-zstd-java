package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

/**
 * A Zstandard streaming decompressor for Discord gateway messages.
 * <br>Each message needs to be passed in the same order they were received, none must be skipped.
 *
 * <p>Note: Instances are <b>not</b> thread safe, as there should be an instance per gateway connection, which uses 1 read thread.
 */
@NullMarked
public interface ZstdDecompressor {
    int MIN_BUFFER_SIZE = 1024;

    void reset();

    void close();

    byte[] decompress(byte[] data);
}
