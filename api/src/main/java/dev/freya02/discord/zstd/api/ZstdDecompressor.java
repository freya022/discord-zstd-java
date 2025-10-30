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
