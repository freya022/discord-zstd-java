package dev.freya02.discord.zstd.jda;

import dev.freya02.discord.zstd.api.DiscordZstdContext;
import dev.freya02.discord.zstd.api.DiscordZstdException;
import net.dv8tion.jda.api.exceptions.DecompressionException;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@NullMarked
public class ZstdStreamedTransportGatewayDecompressorTest {
    @SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
    @Test
    public void testDecompressionErrorThrowsDecompressionException() {
        var context = Mockito.mock(DiscordZstdContext.class);

        var stream = new InputStream() {
            @Override
            public int read() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("Must be unwrapped", new DiscordZstdException("Expected"));
            }
        };
        doReturn(stream).when(context).createInputStream(any());

        var decompressor = new ZstdStreamedTransportGatewayDecompressor(context);

        assertThatExceptionOfType(DecompressionException.class).isThrownBy(() -> decompressor.createInputStream(new byte[0]).read(new byte[0]));
    }
}
