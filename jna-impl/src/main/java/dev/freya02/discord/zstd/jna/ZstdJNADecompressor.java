package dev.freya02.discord.zstd.jna;

import com.sun.jna.Pointer;
import dev.freya02.discord.zstd.api.DiscordZstdException;
import dev.freya02.discord.zstd.internal.AbstractZstdDecompressor;
import dev.freya02.discord.zstd.internal.Checks;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NullMarked
public class ZstdJNADecompressor extends AbstractZstdDecompressor {

    private static final Logger LOG = LoggerFactory.getLogger(ZstdJNADecompressor.class);

    private final Pointer stream;
    private final ZstdJna.ZSTD_outBuffer outputSegment;

    private boolean invalidated = false;
    private boolean closed = false;

    protected ZstdJNADecompressor(int bufferSizeHint)
    {
        this.stream = ZstdJna.INSTANCE.ZSTD_createDStream();

        int bufferSize = bufferSizeHint == ZSTD_RECOMMENDED_BUFFER_SIZE
                ? Math.toIntExact(ZstdJna.INSTANCE.ZSTD_DStreamOutSize())
                : bufferSizeHint;

        outputSegment = new ZstdJna.ZSTD_outBuffer(bufferSize);

        reset();
    }

    @Override
    public void reset()
    {
        if (closed)
            throw new IllegalStateException("Decompressor is closed");

        ZstdJna.INSTANCE.ZSTD_initDStream(stream);
        invalidated = false;
    }

    @Override
    public void close()
    {
        if (closed)
            return;

        closed = true;
        ZstdJna.INSTANCE.ZSTD_freeDStream(stream);
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
        Checks.notNull(data, "Data");

        if (LOG.isTraceEnabled())
            LOG.trace("Decompressing data {}", Arrays.toString(data));

        List<byte[]> chunks = new ArrayList<>();

        // Copy compressed data to native memory
        final ZstdJna.ZSTD_inBuffer inputSegment = new ZstdJna.ZSTD_inBuffer(data);

        while (true) {
            // In cases where the output buffer is too small for the decompressed input,
            // we'll loop back, so, reset the output position
            outputSegment.pos = 0;
            outputSegment.write();

            // To compare whether Zstd consumed input
            long previousInputOffset = inputSegment.pos;

            final long result = ZstdJna.INSTANCE.ZSTD_decompressStream(stream, outputSegment, inputSegment);
            inputSegment.read();
            outputSegment.read();

            final byte[] bytes = new byte[Math.toIntExact(outputSegment.pos)];
            outputSegment.dst.get(bytes);
            outputSegment.dst.position(0);

            boolean madeForwardProgress = inputSegment.pos > previousInputOffset || outputSegment.pos > 0;
            boolean fullyProcessedInput = inputSegment.pos == data.length;

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
                if (ZstdJna.INSTANCE.ZSTD_isError(result) > 0) {
                    throw createException(ZstdJna.INSTANCE.ZSTD_getErrorName(result));
                } else {
                    throw createException(String.format("Unexpected result: %d, is error: %s", result, ZstdJna.INSTANCE.ZSTD_isError(result)));
                }
            }
        }
    }

    private DiscordZstdException createException(String message) {
        invalidated = true;
        return new DiscordZstdException(message);
    }
}
