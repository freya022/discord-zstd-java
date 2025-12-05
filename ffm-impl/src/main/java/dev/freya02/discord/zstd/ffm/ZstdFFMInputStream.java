package dev.freya02.discord.zstd.ffm;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

@NullMarked
class ZstdFFMInputStream extends InputStream {

    private final ZstdFFMContext context;

    private final MemorySegment input;
    private final long inputSize;
    private final MemorySegment inputPos;

    private final MemorySegment bufferPos;

    private boolean closed = false;

    protected ZstdFFMInputStream(ZstdFFMContext context, byte[] input) {
        this.context = context;

        final Arena arena = Arena.ofAuto();

        this.input = MemorySegment.ofArray(input);
        this.inputSize = input.length;
        this.inputPos = arena.allocate(ValueLayout.JAVA_LONG);
        setLong(inputPos, 0);

        this.bufferPos = arena.allocate(ValueLayout.JAVA_LONG);
    }

    @Override
    public void close() {
        if (closed)
            return;

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
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0)
            return 0;

        setLong(bufferPos, 0);

        // To compare whether Zstd consumed input
        long previousInputOffset = getLong(inputPos);

        context.decompress(MemorySegment.ofArray(b).asSlice(off), len, bufferPos, input, inputSize, inputPos);
        // No need to read buffer, the backing array has been updated

        boolean madeForwardProgress = getLong(inputPos) > previousInputOffset || getLong(bufferPos) > 0;
        boolean fullyProcessedInput = getLong(inputPos) == inputSize;

        if (!madeForwardProgress && fullyProcessedInput) {
            return -1; // EOF
        } else {
            return (int) getLong(bufferPos);
        }
    }

    protected static void setLong(MemorySegment segment, long value) {
        segment.set(ValueLayout.JAVA_LONG, 0, value);
    }

    protected static long getLong(MemorySegment segment) {
        return segment.get(ValueLayout.JAVA_LONG, 0);
    }
}
