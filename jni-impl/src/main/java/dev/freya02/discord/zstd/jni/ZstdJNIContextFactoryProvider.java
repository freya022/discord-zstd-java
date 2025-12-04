package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdContextFactory;
import dev.freya02.discord.zstd.api.ZstdContextFactoryProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNIContextFactoryProvider implements ZstdContextFactoryProvider {
    @Override
    public ZstdContextFactory get() {
        return new ZstdJNIContextFactory();
    }
}
