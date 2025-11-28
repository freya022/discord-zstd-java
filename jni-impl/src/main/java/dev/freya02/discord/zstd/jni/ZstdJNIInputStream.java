package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdException;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;

@NullMarked
public class ZstdJNIInputStream extends InputStream {

    private final ZstdJNIContext context;
    private final long nativeContextPtr;

    private final byte[] input;
    private final long inputSize;

    private boolean closed = false;

    protected ZstdJNIInputStream(ZstdJNIContext context, long zds, byte[] input) {
        this.context = context;
        this.nativeContextPtr = newContext(zds);
        this.input = input;
        this.inputSize = input.length;
    }

    @Override
    public void close() {
        if (closed)
            return;
        freeContext(nativeContextPtr);

        closed = true;
    }

    @Override
    public int read() throws IOException {
        final byte[] singleByte = new byte[1];
        return read(singleByte, 0, 1) <= 0 ? -1 : Byte.toUnsignedInt(singleByte[0]);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed)
            throw new IOException("Stream is closed");
        context.checkValid();
        if ((b.length | off | len) < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();
        if (len == 0)
            return 0;

        try {
            return (int) inflate0(nativeContextPtr, input, inputSize, b, off, len);
        } catch (ZstdException e) {
            context.invalidate();
            throw new RuntimeException(e);
        }
    }

    private static native long newContext(long zdsPtr);

    private static native long freeContext(long ctxPtr);

    private static native long inflate0(long ctxPtr,
                                        byte[] src, long srcSize,
                                        byte[] dst, long dstOff, long dstSize);
}
