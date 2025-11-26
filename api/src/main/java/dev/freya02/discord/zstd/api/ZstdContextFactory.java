package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ZstdContextFactory {
    ZstdContext create();
}
