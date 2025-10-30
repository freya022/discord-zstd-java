package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Path;

@NullMarked
public class ZstdNativesLoader {
    private static boolean init = false;

    public static synchronized boolean isLoaded() {
        return init;
    }

    public static synchronized boolean load(Path path) {
        if (init)
            return false;

        final String pathStr = path.toAbsolutePath().toString();
        System.setProperty("zstd.lib", pathStr);
        System.load(pathStr);
        init = true;
        return true;
    }

    public static synchronized boolean loadFromJar() throws IOException {
        if (init)
            return false;

        String architecture = NativeUtil.getCanonicalArchitecture(System.getProperty("os.arch"));
        String osName = System.getProperty("os.name");

        String platform;
        String extension;
        if (osName.startsWith("Linux")) {
            platform = "linux-" + architecture;
            extension = "so";
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            platform = "darwin";
            extension = "dylib";
        } else if (osName.startsWith("Windows")) {
            platform = "win32-" + architecture;
            extension = "dll";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        String resourcePath = String.format("/natives/%s/libzstd.%s", platform, extension);
        Path nativePath = NativeUtil.copyNativeFromJar(resourcePath, ZstdNativesLoader.class);
        load(nativePath);
        return true;
    }

    public static synchronized boolean loadFromJar(String resourcePath, Class<?> clazz) throws IOException {
        if (init)
            return false;

        Path nativePath = NativeUtil.copyNativeFromJar(resourcePath, clazz);
        load(nativePath);
        return true;
    }
}
