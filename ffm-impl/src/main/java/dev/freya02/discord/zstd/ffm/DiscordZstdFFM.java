package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.DiscordZstd;
import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@NullMarked
public class DiscordZstdFFM implements DiscordZstd {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordZstdFFM.class);

    public DiscordZstdFFM() throws IOException {
        LOGGER.debug("Using FFM implementation of discord-zstd-java");

        // Load natives if they weren't already
        ZstdNativesLoader.loadFromJar();
    }

    @Override
    public ZstdContext createContext() {
        return new ZstdFFMContext();
    }

    @Override
    public ZstdDecompressorFactory createDecompressorFactory(int bufferSizeHint) {
        return new ZstdFFMDecompressorFactory(bufferSizeHint);
    }
}
