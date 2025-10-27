package dev.freya02.discord.zstd;

import javax.annotation.Nonnull;

public interface ZstdDecompressorFactory {
    @Nonnull
    ZstdDecompressor get(int maxBufferSize);
}
