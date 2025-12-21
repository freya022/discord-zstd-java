package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.io.InputStream;

/**
 * Wraps a Zstandard decompression context, it keeps track of the previous decompressed data,
 * allowing for greater compression ratios, but the data must be decompressed in the same order.
 *
 * <br>It can be used to decompress-as-you-consume with {@link InputStream InputStreams}.
 *
 * <p>Instances are <b>not</b> thread safe.
 */
@NullMarked
public interface DiscordZstdContext {
    /**
     * Closes the context and frees resources.
     *
     * <p>If this is already closed, this is a no-op.
     */
    void close();

    /**
     * Resets the current context so it can be used again.
     *
     * @throws IllegalStateException
     *         If the context is closed
     */
    void reset();

    /**
     * Exceptions thrown by the {@link InputStream} will invalidate the decompression context,
     * in which case you will need to recreate a new one or call {@link #reset()},
     * as well as recreate a gateway connection.
     *
     * @param  input
     *         The data to decompress
     *
     * @throws IllegalArgumentException
     *         If the provided input is {@code null}
     * @throws IllegalStateException
     *         If the context is closed or in an errored state
     *
     * @return The new {@link InputStream}
     */
    InputStream createInputStream(byte[] input);
}
