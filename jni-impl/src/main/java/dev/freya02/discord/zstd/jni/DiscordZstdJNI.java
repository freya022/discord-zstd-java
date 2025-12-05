package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.*;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@NullMarked
public class DiscordZstdJNI implements DiscordZstd {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordZstdJNI.class);

    public DiscordZstdJNI() throws IOException {
        LOGGER.debug("Using JNI implementation of discord-zstd-java");

        // Load natives if they weren't already
        ZstdNativesLoader.loadFromJar();
    }

    @Override
    public ZstdContext createContext() {
        return new ZstdJNIContext();
    }

    @Override
    public ZstdDecompressorFactory createDecompressorFactory(int bufferSizeHint) {
        if (bufferSizeHint < ZstdDecompressor.MIN_BUFFER_SIZE && bufferSizeHint != ZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be larger than or equal to " + ZstdDecompressor.MIN_BUFFER_SIZE + ", provided " + bufferSizeHint);
        return new ZstdJNIDecompressorFactory(bufferSizeHint);
    }
}
