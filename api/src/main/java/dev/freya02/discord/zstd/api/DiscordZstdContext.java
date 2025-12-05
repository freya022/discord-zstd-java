package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.io.InputStream;

@NullMarked
public interface DiscordZstdContext {
    void close();

    void reset();

    InputStream createInputStream(byte[] input);
}
