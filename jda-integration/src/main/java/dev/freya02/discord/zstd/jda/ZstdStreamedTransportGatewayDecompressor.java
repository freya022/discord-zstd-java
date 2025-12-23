package dev.freya02.discord.zstd.jda;

import dev.freya02.discord.zstd.api.DiscordZstdContext;
import dev.freya02.discord.zstd.api.DiscordZstdException;
import net.dv8tion.jda.api.exceptions.DecompressionException;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@NullMarked
public class ZstdStreamedTransportGatewayDecompressor implements GatewayDecompressor.Transport.Streamed {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZstdStreamedTransportGatewayDecompressor.class);

    private final DiscordZstdContext context;

    public ZstdStreamedTransportGatewayDecompressor(DiscordZstdContext context) {
        this.context = context;
    }

    @Nullable
    @Override
    public String getQueryParameter() {
        return "zstd-stream";
    }

    @Override
    public void reset() {
        context.reset();
    }

    @Override
    public void shutdown() {
        context.close();
    }

    @Override
    public InputStream createInputStream(byte[] data) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Decompressing data {}", Arrays.toString(data));
        }

        return new GatewayInputStream(context.createInputStream(data));
    }

    private static class GatewayInputStream extends FilterInputStream {

        private GatewayInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                return super.read(b, off, len);
            } catch (IOException e) {
                Throwable cause = e.getCause();
                if (cause instanceof DiscordZstdException) {
                    throw new DecompressionException(e);
                }

                throw e;
            }
        }
    }
}
