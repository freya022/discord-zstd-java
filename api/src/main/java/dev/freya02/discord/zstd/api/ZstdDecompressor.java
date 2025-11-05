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
    /**
     * The "recommended" buffer size as defined by {@code ZSTD_DStreamOutSize()} (128 KB as of v1.5.7). This isn't a default.
     *
     * <p>For the use case of Discord's gateway,
     * the value returned is overkill as it would mean <b>at least</b> 128 KB (Zstd's context has even more data) allocated for every shard,
     * for this reason we recommend you to stick to the default buffer size,
     * as it should be enough for most gateway messages without requiring multiple passes.
     */
    int ZSTD_RECOMMENDED_BUFFER_SIZE = -2; // Reserve -1 for defaults of libraries

    /** The default buffer size for decompression, 8 KB */
    int DEFAULT_BUFFER_SIZE = 8192;

    /** The minimum buffer size for decompression, 1 KB */
    int MIN_BUFFER_SIZE = 1024;

    /**
     * Resets the decompressor, the next decompressed message must be the first message of the Zstd stream,
     * meaning you will have to close your websocket connection and start a new one.
     *
     * <p>If this decompressor was in an errored state, calling this will render it usable again.
     *
     * @throws IllegalStateException
     *         If this decompressor is closed
     */
    void reset();

    /**
     * Closes the decompressor and frees the associated resources.
     * <br>A new decompressor needs be created for further operations.
     *
     * <p>If this decompressor is already closed, this is a no-op.
     */
    void close();

    /**
     * Decompresses the provided data.
     *
     * <p>Each Discord gateway message is a full websocket message,
     * as such, you can pass the binary data from your websocket directly to this method,
     * no buffering required.
     *
     * @throws IllegalArgumentException
     *         If the passed data is {@code null}
     * @throws IllegalStateException
     *         If this decompressor is closed,
     *         or if the decompressor is an errored state and needs to be {@linkplain #reset() reset}
     * @throws ZstdException
     *         If Zstd was unable to decompress the data for any reason, if this exception occurs,
     *         the decompressor will be in an errored state and will need to be {@linkplain #reset() reset}
     */
    byte[] decompress(byte[] data);
}
