package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.DiscordZstd;
import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
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
        return new ZstdJNIDecompressorFactory(bufferSizeHint);
    }
}
