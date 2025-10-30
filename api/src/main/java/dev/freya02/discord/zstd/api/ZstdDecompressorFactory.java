package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.util.ServiceLoader;

/**
 * Factory of {@link ZstdDecompressor}.
 *
 * <p>Instances are thread safe and can be obtained using {@link ServiceLoader}.
 */
@NullMarked
public interface ZstdDecompressorFactory {
    /**
     * Creates a new {@link ZstdDecompressor} instance with the provided decompression buffer size.
     *
     * @param  bufferSize
     *         The size of the buffer used for decompression, must be larger than {@link ZstdDecompressor#MIN_BUFFER_SIZE}.
     *         Typically, bigger buffers mean less decompression loops, it does not change inputs or outputs
     *
     * @throws IllegalArgumentException
     *         If {@code bufferSize} is less than {@value ZstdDecompressor#MIN_BUFFER_SIZE}
     *
     * @return A new {@link ZstdDecompressor} instance
     */
    ZstdDecompressor get(int bufferSize);
}
