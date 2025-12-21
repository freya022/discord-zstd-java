package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.DiscordZstdException;
import dev.freya02.discord.zstd.internal.AbstractZstdDecompressor;
import dev.freya02.discord.zstd.internal.Checks;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@NullMarked
class DiscordZstdJNIDecompressor extends AbstractZstdDecompressor {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordZstdJNIDecompressor.class);

    private final long zds;
    private final byte[] buffer;

    private boolean invalidated = false;
    private boolean closed = false;

    protected DiscordZstdJNIDecompressor(int bufferSizeHint)
    {
        this.zds = createDStream();

        int bufferSize = bufferSizeHint == ZSTD_RECOMMENDED_BUFFER_SIZE
                ? Math.toIntExact(DStreamOutSize())
                : bufferSizeHint;

        this.buffer = new byte[bufferSize];

        reset();
    }

    @Override
    public void reset()
    {
        if (closed)
            throw new IllegalStateException("Decompressor is closed");

        initDStream(zds);
        invalidated = false;
    }

    @Override
    public void close()
    {
        if (closed)
            return;

        closed = true;
        freeDStream(zds);
    }

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

        try {
            return decompressMessage(zds, buffer, data);
        } catch (DiscordZstdException e) {
            invalidated = true;
            throw e;
        }
    }

    private static native long createDStream();
    private static native long freeDStream(long zds);
    private static native int DStreamOutSize();
    private static native long initDStream(long zds);
    private static native byte[] decompressMessage(long zds, byte[] buffer, byte[] input);
}
