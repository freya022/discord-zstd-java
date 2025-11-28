package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdContext;
import org.jspecify.annotations.NullMarked;

import java.io.InputStream;

@NullMarked
public class ZstdJNIContext implements ZstdContext {
    private final long zds;

    private boolean invalidated = false;
    private boolean closed = false;

    public ZstdJNIContext() {
        this.zds = createDStream();
    }

    public void invalidate() {
        invalidated = true;
    }

    public void checkValid() {
        if (closed)
            throw new IllegalStateException("Context is closed");
        if (invalidated)
            throw new IllegalStateException("Context is in an errored state and needs to be reset");
    }

    @Override
    public void close() {
        if (closed)
            return;
        closed = true;

        freeDStream(zds);
    }

    @Override
    public void reset() {
        if (closed)
            throw new IllegalStateException("Context is closed");

        initDStream(zds);
        invalidated = false;
    }

    @Override
    public InputStream createInputStream(byte[] input) {
        return new ZstdJNIInputStream(this, zds, input);
    }

    private static native long createDStream();

    private static native void freeDStream(long zds);

    private static native long initDStream(long zds);
}
