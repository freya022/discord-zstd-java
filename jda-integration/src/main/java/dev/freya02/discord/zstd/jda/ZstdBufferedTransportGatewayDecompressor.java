package dev.freya02.discord.zstd.jda;

import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdException;
import net.dv8tion.jda.api.exceptions.DecompressionException;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@NullMarked
public class ZstdBufferedTransportGatewayDecompressor implements GatewayDecompressor.Transport.Buffered {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZstdBufferedTransportGatewayDecompressor.class);

    private final DiscordZstdDecompressor decompressor;

    public ZstdBufferedTransportGatewayDecompressor(DiscordZstdDecompressor decompressor) {
        this.decompressor = decompressor;
    }

    @Nullable
    @Override
    public String getQueryParameter() {
        return "zstd-stream";
    }

    @Override
    public void reset() {
        decompressor.reset();
    }

    @Override
    public void shutdown() {
        decompressor.close();
    }

    @Override
    public byte[] decompress(byte[] data) throws DecompressionException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Decompressing data {}", Arrays.toString(data));
        }

        try {
            return decompressor.decompress(data);
        } catch (DiscordZstdException e) {
            throw new DecompressionException(e);
        }
    }
}
