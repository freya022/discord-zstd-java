package dev.freya02.discord.zstd.jna;

import com.sun.jna.Pointer;
import dev.freya02.discord.zstd.AbstractZstdDecompressor;
import dev.freya02.discord.zstd.ZstdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZstdJNADecompressor extends AbstractZstdDecompressor {
    private static final Logger LOG = LoggerFactory.getLogger(ZstdJNADecompressor.class);

    private final Pointer stream;
    private final ZstdJna.ZSTD_outBuffer outputSegment;

    private boolean shutdown = false;

    public ZstdJNADecompressor(int maxBufferSize)
    {
        this.stream = ZstdJna.INSTANCE.ZSTD_createDStream();

        outputSegment = new ZstdJna.ZSTD_outBuffer(maxBufferSize);

        reset();
    }

    @Override
    public void reset()
    {
        ZstdJna.INSTANCE.ZSTD_initDStream(stream);
    }

    @Override
    public void shutdown()
    {
        shutdown = true;
        ZstdJna.INSTANCE.ZSTD_freeDStream(stream);
        // outputSegment is managed by GC
    }

    /**
     * As each gateway message is a WebSocket message,
     * the input data can be decompressed (sometimes in multiple rounds due to output buffer size limits)
     * and always return non-null decompressed data
     */
    @Nonnull
    @Override
    public byte[] decompress(@Nonnull byte[] data)
    {
        if (shutdown)
            throw new IllegalStateException("Decompressor has shut down");

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
                    throw new ZstdException("Malformed");
                }

                chunks.add(bytes);
                // No need to save the input offset as we'll pass the same ZSTD_inBuffer struct again
            } else {
                if (ZstdJna.INSTANCE.ZSTD_isError(result) > 0) {
                    throw new ZstdException(ZstdJna.INSTANCE.ZSTD_getErrorName(result));
                } else {
                    throw new ZstdException(String.format("Unexpected result: %d, is error: %s", result, ZstdJna.INSTANCE.ZSTD_isError(result)));
                }
            }
        }
    }
}
