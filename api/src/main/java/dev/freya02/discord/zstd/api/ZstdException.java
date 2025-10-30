package dev.freya02.discord.zstd.api;

/**
 * An exception thrown when Zstd returns an error code.
 */
public class ZstdException extends RuntimeException {
    public ZstdException(String message) {
        super(message);
    }
}
