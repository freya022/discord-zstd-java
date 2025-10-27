package dev.freya02.discord.zstd;

import javax.annotation.Nonnull;

public interface ZstdDecompressor {
    void reset();

    void close();

    @Nonnull
    byte[] decompress(@Nonnull byte[] data);
}
