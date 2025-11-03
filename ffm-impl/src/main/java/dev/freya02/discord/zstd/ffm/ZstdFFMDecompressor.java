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
public class ZstdFFMDecompressor extends AbstractZstdDecompressor {

    private static final Logger LOG = LoggerFactory.getLogger(ZstdFFMDecompressor.class);

    private final MemorySegment stream;
    private final MemorySegment outputSegment;

    private boolean invalidated = false;
    private boolean closed = false;

    public ZstdFFMDecompressor(int bufferSize)
    {
        if (bufferSize < MIN_BUFFER_SIZE && bufferSize != DEFAULT_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be higher than or equal to " + MIN_BUFFER_SIZE + ", provided " + bufferSize);

        this.stream = Zstd.ZSTD_createDStream();
        if (bufferSize == DEFAULT_BUFFER_SIZE)
            bufferSize = Math.toIntExact(Zstd.ZSTD_DStreamOutSize());

        final Arena arena = Arena.ofAuto();
        outputSegment = ZSTD_outBuffer.allocate(arena);
        ZSTD_outBuffer.dst(outputSegment, arena.allocate(bufferSize));
        ZSTD_outBuffer.size(outputSegment, bufferSize);
        ZSTD_outBuffer.pos(outputSegment, 0);

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
            final MemorySegment inputSegment = ZSTD_inBuffer.allocate(arena);
            ZSTD_inBuffer.src(inputSegment, arena.allocateFrom(ValueLayout.JAVA_BYTE, data));
            ZSTD_inBuffer.size(inputSegment, data.length);

            while (true) {
                // In cases where the output buffer is too small for the decompressed input,
                // we'll loop back, so, reset the output position
                ZSTD_outBuffer.pos(outputSegment, 0);

                // To compare whether Zstd consumed input
                long previousInputOffset = ZSTD_inBuffer.pos(inputSegment);

                final long result = Zstd.ZSTD_decompressStream(stream, outputSegment, inputSegment);
                final byte[] bytes = ZSTD_outBuffer.dst(outputSegment)
                        .reinterpret(ZSTD_outBuffer.pos(outputSegment))
                        .toArray(ValueLayout.JAVA_BYTE);

                boolean madeForwardProgress = ZSTD_inBuffer.pos(inputSegment) > previousInputOffset || ZSTD_outBuffer.pos(outputSegment) > 0;
                boolean fullyProcessedInput = ZSTD_inBuffer.pos(inputSegment) == data.length;

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

    private ZstdException createException(String message) {
        invalidated = true;
        return new ZstdException(message);
    }
}
