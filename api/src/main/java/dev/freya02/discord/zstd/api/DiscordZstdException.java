package dev.freya02.discord.zstd.api;

/**
 * An exception thrown when Zstd returns an error code.
 */
public class DiscordZstdException extends RuntimeException {
    public DiscordZstdException(String message) {
        super(message);
    }
}
