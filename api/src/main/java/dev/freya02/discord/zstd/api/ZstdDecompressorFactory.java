package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ZstdDecompressorFactory {
    ZstdDecompressor get(int maxBufferSize);
}
