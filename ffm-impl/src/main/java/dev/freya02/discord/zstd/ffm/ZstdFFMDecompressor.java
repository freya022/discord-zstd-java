package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdException;
import dev.freya02.discord.zstd.internal.AbstractZstdDecompressor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NullMarked
class ZstdFFMDecompressor extends AbstractZstdDecompressor {

    private static final Logger LOG = LoggerFactory.getLogger(ZstdFFMDecompressor.class);

    private final MemorySegment stream;
    private final MemorySegment dstSegment;
    private final long dstSize;
    private final MemorySegment dstPosSegment;

    private boolean invalidated = false;
    private boolean closed = false;

    protected ZstdFFMDecompressor(int bufferSizeHint)
    {
        this.stream = Zstd.ZSTD_createDStream();

        int bufferSize = bufferSizeHint == ZSTD_RECOMMENDED_BUFFER_SIZE
                ? Math.toIntExact(Zstd.ZSTD_DStreamOutSize())
                : bufferSizeHint;

        final Arena arena = Arena.ofAuto();
        this.dstSegment = arena.allocate(ValueLayout.JAVA_BYTE, bufferSize);
        this.dstSize = bufferSize;
        this.dstPosSegment = arena.allocate(ValueLayout.JAVA_LONG);
        setLong(dstPosSegment, 0);

        reset();
    }

    @Override
    public void reset()
    {
        if (closed)
            throw new IllegalStateException("Decompressor is closed");

        Zstd.ZSTD_initDStream(stream);
        invalidated = false;
    }

    @Override
    public void close()
    {
        if (closed)
            return;

        closed = true;
        Zstd.ZSTD_freeDStream(stream);
        // outputSegment is managed by GC
    }

    /*
     * As each gateway message is a WebSocket message,
     * the input data can be decompressed (sometimes in multiple rounds due to output buffer size limits)
     * and always return non-null decompressed data
     */
    @Override
    public byte[] decompress(byte[] data)
    {
        if (closed)
            throw new IllegalStateException("Decompressor is closed");
        if (invalidated)
            throw new IllegalStateException("Decompressor is in an errored state and needs to be reset");
        //noinspection ConstantValue
        if (data == null)
            throw new IllegalArgumentException("data is null");

        if (LOG.isTraceEnabled())
            LOG.trace("Decompressing data {}", Arrays.toString(data));

        List<byte[]> chunks = new ArrayList<>();
        try (Arena arena = Arena.ofConfined())
        {
            // Copy compressed data to native memory
            final MemorySegment inputSegment = MemorySegment.ofArray(data);
            final long inputSize = data.length;
            final MemorySegment inputPos = arena.allocate(ValueLayout.JAVA_LONG);

            while (true) {
                // In cases where the output buffer is too small for the decompressed input,
                // we'll loop back, so, reset the output position
                setLong(dstPosSegment, 0);

                // To compare whether Zstd consumed input
                long previousInputOffset = getLong(inputPos);

                final long result = Zstd.ZSTD_decompressStream_simpleArgs(stream, dstSegment, dstSize, dstPosSegment, inputSegment, inputSize, inputPos);
                final byte[] bytes = dstSegment
                        .reinterpret(getLong(dstPosSegment))
                        .toArray(ValueLayout.JAVA_BYTE);

                boolean madeForwardProgress = getLong(inputPos) > previousInputOffset || getLong(dstPosSegment) > 0;
                boolean fullyProcessedInput = getLong(inputPos) == data.length;

                // Only merge when no input was consumed,
                // Zstd may have decompressed data in its buffers that it will hand off to us without consuming input
                if (result == 0 || (!madeForwardProgress && fullyProcessedInput)) {
                    // Completely decoded and flushed
                    return mergeChunks(chunks, bytes);
                } else if (result > 0) {
                    if (!fullyProcessedInput && !madeForwardProgress) {
                        throw createException("Malformed");
                    }

                    chunks.add(bytes);
                    // No need to save the input offset as we'll pass the same ZSTD_inBuffer struct again
                } else {
                    if (Zstd.ZSTD_isError(result) > 0) {
                        throw createException(Zstd.ZSTD_getErrorName(result).getString(0));
                    } else {
                        throw createException("Unexpected result: %d, is error: %s".formatted(result, Zstd.ZSTD_isError(result)));
                    }
                }
            }
        }
    }

    private static void setLong(MemorySegment segment, long value) {
        segment.set(ValueLayout.JAVA_LONG, 0, value);
    }

    private static long getLong(MemorySegment segment) {
        return segment.get(ValueLayout.JAVA_LONG, 0);
    }

    private ZstdException createException(String message) {
        invalidated = true;
        return new ZstdException(message);
    }
}
