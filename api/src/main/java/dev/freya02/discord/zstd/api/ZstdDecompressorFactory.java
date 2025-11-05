package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

/**
 * Factory of {@link ZstdDecompressor}.
 *
 * <p>Instances are thread safe.
 */
@NullMarked
public interface ZstdDecompressorFactory {
    /**
     * Creates a new {@link ZstdDecompressor} configured with the parameters passed to this factory.
     *
     * @return A new {@link ZstdDecompressor} instance
     */
    ZstdDecompressor create();
}
