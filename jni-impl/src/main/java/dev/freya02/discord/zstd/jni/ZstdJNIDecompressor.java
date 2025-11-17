package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.internal.AbstractZstdDecompressor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@NullMarked
public class ZstdJNIDecompressor extends AbstractZstdDecompressor {

    private static final Logger LOG = LoggerFactory.getLogger(ZstdJNIDecompressor.class);

    private final long zds;

    private boolean invalidated = false;
    private boolean closed = false;

    protected ZstdJNIDecompressor(int bufferSizeHint)
    {
        this.zds = createDStream();

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
        //noinspection ConstantValue
        if (data == null)
            throw new IllegalArgumentException("data is null");

        if (LOG.isTraceEnabled())
            LOG.trace("Decompressing data {}", Arrays.toString(data));

        return decompressMessage(zds, data);
    }

    private static native long createDStream();
    private static native long freeDStream(long zds);
    private static native int DStreamOutSize();
    private static native long initDStream(long zds);
    private static native byte[] decompressMessage(long zds, byte[] input);
}
