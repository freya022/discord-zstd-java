package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdContextFactory;
import dev.freya02.discord.zstd.api.ZstdContextFactoryProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdFFMContextFactoryProvider implements ZstdContextFactoryProvider {
    @Override
    public ZstdContextFactory get() {
        return new ZstdFFMContextFactory();
    }
}
