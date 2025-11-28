package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdContextFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNIContextFactory implements ZstdContextFactory {
    @Override
    public ZstdContext create() {
        return new ZstdJNIContext();
    }
}
