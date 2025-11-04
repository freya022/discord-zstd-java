package dev.freya02.discord.zstd.internal;

import dev.freya02.discord.zstd.api.ZstdDecompressor;

import java.util.List;

public abstract class AbstractZstdDecompressor implements ZstdDecompressor {

    protected AbstractZstdDecompressor() {
    }

    protected static byte[] mergeChunks(List<byte[]> chunks, byte[] bytes) {
        int totalLength = 0;
        totalLength += bytes.length;
        for (byte[] chunk : chunks)
            totalLength += chunk.length;

        byte[] data = new byte[totalLength];
        int copyOffset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, data, copyOffset, chunk.length);
            copyOffset += chunk.length;
        }
        System.arraycopy(bytes, 0, data, copyOffset, bytes.length);

        return data;
    }
}
