package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdContextFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdFFMContextFactory implements ZstdContextFactory {
    @Override
    public ZstdContext create() {
        return new ZstdFFMContext();
    }
}
