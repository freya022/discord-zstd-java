package dev.freya02.discord.zstd;

import javax.annotation.Nonnull;

public interface ZstdDecompressor {
    void reset();

    void shutdown();

    @Nonnull
    byte[] decompress(@Nonnull byte[] data);
}
