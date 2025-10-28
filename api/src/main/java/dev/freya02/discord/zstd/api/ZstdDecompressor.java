package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ZstdDecompressor {
    void reset();

    void close();

    byte[] decompress(byte[] data);
}
