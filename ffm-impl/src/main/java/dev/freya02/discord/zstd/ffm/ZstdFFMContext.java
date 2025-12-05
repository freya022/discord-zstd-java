package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdException;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;
import java.lang.foreign.MemorySegment;

@NullMarked
class ZstdFFMContext implements ZstdContext {
    private final MemorySegment stream;

    private boolean invalidated = false;
    private boolean closed = false;

    public ZstdFFMContext() {
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
        return new ZstdFFMInputStream(this, input);
    }

    public void decompress(MemorySegment dst, long dstCapacity, MemorySegment dstPos, MemorySegment src, long srcSize, MemorySegment srcPos) {
        if (closed)
            throw new IllegalStateException("Context is closed");
        if (invalidated)
            throw new IllegalStateException("Context is in an errored state and needs to be reset");

        long result = Zstd.ZSTD_decompressStream_simpleArgs(stream, dst, dstCapacity, dstPos, src, srcSize, srcPos);

        if (Zstd.ZSTD_isError(result) > 0) {
            throw createException(Zstd.ZSTD_getErrorName(result).getString(0));
        }
    }

    public ZstdException createException(String message) {
        invalidated = true;
        return new ZstdException(message);
    }
}
