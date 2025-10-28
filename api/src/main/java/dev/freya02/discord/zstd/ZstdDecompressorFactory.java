package dev.freya02.discord.zstd;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ZstdDecompressorFactory {
    ZstdDecompressor get(int maxBufferSize);
}
