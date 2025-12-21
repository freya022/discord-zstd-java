package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.DiscordZstdContext;
import dev.freya02.discord.zstd.api.DiscordZstdException;
import dev.freya02.discord.zstd.internal.Checks;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;
import java.lang.foreign.MemorySegment;

@NullMarked
class DiscordZstdFFMContext implements DiscordZstdContext {
    private final MemorySegment stream;

    private boolean invalidated = false;
    private boolean closed = false;

    public DiscordZstdFFMContext() {
        this.stream = Zstd.ZSTD_createDStream();
    }

    @Override
    public void close() {
        if (closed)
            return;
        closed = true;

        Zstd.ZSTD_freeDStream(stream);
    }

    @Override
    public void reset() {
        if (closed)
            throw new IllegalStateException("Context is closed");

        Zstd.ZSTD_initDStream(stream);
        invalidated = false;
    }

    @Override
    public InputStream createInputStream(byte[] input) {
        checkValid();
        Checks.notNull(input, "Input");
        return new DiscordZstdFFMInputStream(this, input);
    }

    public void decompress(MemorySegment dst, long dstCapacity, MemorySegment dstPos, MemorySegment src, long srcSize, MemorySegment srcPos) {
        checkValid();

        long result = Zstd.ZSTD_decompressStream_simpleArgs(stream, dst, dstCapacity, dstPos, src, srcSize, srcPos);

        if (Zstd.ZSTD_isError(result) > 0) {
            throw createException(Zstd.ZSTD_getErrorName(result).getString(0));
        }
    }

    public DiscordZstdException createException(String message) {
        invalidated = true;
        return new DiscordZstdException(message);
    }

    private void checkValid() {
        if (closed)
            throw new IllegalStateException("Context is closed");
        if (invalidated)
            throw new IllegalStateException("Context is in an errored state and needs to be reset");
    }
}
