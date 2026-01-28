package dev.freya02.discord.zstd.jda;

import dev.freya02.discord.zstd.api.*;
import net.dv8tion.jda.api.exceptions.DecompressionException;
import net.dv8tion.jda.api.requests.gateway.compression.GatewayDecompressor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Provides buffered transport-level decompression of gateway messages for the Java Discord API (JDA).
 *
 * @see #supplier(int)
 */
@NullMarked
public class ZstdBufferedTransportGatewayDecompressor implements GatewayDecompressor.Transport.Buffered {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZstdBufferedTransportGatewayDecompressor.class);

    private final DiscordZstdDecompressor decompressor;

    public ZstdBufferedTransportGatewayDecompressor(DiscordZstdDecompressor decompressor) {
        this.decompressor = decompressor;
    }

    /**
     * Creates a supplier of {@link ZstdBufferedTransportGatewayDecompressor} with the provided decompression buffer size.
     *
     * <h4>Buffer sizes</h4>
     * This defines the size, in bytes, of the intermediate buffer used for decompression,
     * larger buffer means less decompression loops at a fixed cost of memory.
     *
     * <ul>
     *     <li>The recommended value is {@value DiscordZstdDecompressor#DEFAULT_BUFFER_SIZE}, as it is sufficient for most Discord payloads</li>
     *     <li>
     *         A value "recommended" by Zstd is set with {@link DiscordZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE};
     *         However it is not recommended for normal use cases, see the docs for more details.
     *     </li>
     *     <li>The minimum is {@value DiscordZstdDecompressor#MIN_BUFFER_SIZE}</li>
     * </ul>
     *
     * @param  bufferSizeHint
     *         The hint or value for the size of the buffer used for decompression
     *
     * @throws IllegalArgumentException
     *         If {@code bufferSize} is less than {@value DiscordZstdDecompressor#MIN_BUFFER_SIZE} and not {@value DiscordZstdDecompressor#ZSTD_RECOMMENDED_BUFFER_SIZE}
     *
     * @return A new supplier of {@link ZstdBufferedTransportGatewayDecompressor}
     */
    public static Supplier<ZstdBufferedTransportGatewayDecompressor> supplier(int bufferSizeHint) {
        DiscordZstd zstd = DiscordZstdProvider.get();
        DiscordZstdDecompressorFactory factory = zstd.createDecompressorFactory(bufferSizeHint);
        return () -> new ZstdBufferedTransportGatewayDecompressor(factory.create());
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
