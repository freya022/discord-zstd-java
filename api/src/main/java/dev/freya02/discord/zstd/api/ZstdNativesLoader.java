package dev.freya02.discord.zstd.api;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class to load Zstd natives.
 *
 * <p>An alternative to load natives is to set the {@code zstd.lib} {@linkplain System#setProperty(String, String) system property}
 * to the <b>absolute</b> path of the native library.
 */
@NullMarked
public class ZstdNativesLoader {
    private static boolean init = false;

    /**
     * Whether natives were already loaded. This only considers natives loaded by this class.
     *
     * @return {@code true} if natives are loaded
     */
    public static synchronized boolean isLoaded() {
        return init;
    }

    /**
     * Loads the natives from the provided path's file.
     *
     * @param  path
     *         The path to the native library
     *
     * @throws IllegalArgumentException
     *         If {@code path} is {@code null}
     *
     * @return {@code true} if the natives were loaded, {@code false} if they already were
     */
    public static synchronized boolean load(Path path) {
        if (init)
            return false;
        //noinspection ConstantValue
        if (path == null)
            throw new IllegalArgumentException("path is null");

        final String pathStr = path.toAbsolutePath().toString();
        System.setProperty("zstd.lib", pathStr);
        System.load(pathStr);
        init = true;
        return true;
    }

    /**
     * Loads the natives from this library's JAR.
     *
     * @throws UnsupportedOperationException
     *         If the current platform (OS + architecture) is not supported by default
     * @throws IOException
     *         When the native extraction fails
     *
     * @return {@code true} if the natives were loaded, {@code false} if they already were
     */
    public static synchronized boolean loadFromJar() throws IOException {
        if (init)
            return false;

        String architecture = NativeUtil.getCanonicalArchitecture(System.getProperty("os.arch"));
        String osName = System.getProperty("os.name");

        String platform;
        String extension;
        if (osName.startsWith("Linux")) {
            if (!architecture.equals("x86-64") && !architecture.equals("aarch64") && !architecture.equals("arm"))
                throw new IllegalStateException("Unsupported architecture: " + architecture);
            platform = "linux-" + architecture;
            extension = "so";
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            platform = "darwin";
            extension = "dylib";
        } else if (osName.startsWith("Windows")) {
            if (!architecture.equals("x86-64") && !architecture.equals("aarch64"))
                throw new IllegalStateException("Unsupported architecture: " + architecture);
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

    /**
     * Loads the natives using the provided path from the provided class.
     *
     * <p>Remember the resource path is relative to the provided class, unless prefixed with {@code /}.
     *
     * @param  resourcePath
     *         The path to the native library in the provided class
     * @param  clazz
     *         The class from which to relatively load the resource from
     *
     * @throws IllegalArgumentException
     *         If {@code resourcePath} or {@code clazz} is {@code null}
     * @throws IOException
     *         If the resource does not exist or when extracting it fails
     *
     * @return {@code true} if the natives were loaded, {@code false} if they already were
     */
    public static synchronized boolean loadFromJar(String resourcePath, Class<?> clazz) throws IOException {
        if (init)
            return false;
        //noinspection ConstantValue
        if (resourcePath == null)
            throw new IllegalArgumentException("resourcePath is null");
        //noinspection ConstantValue
        if (clazz == null)
            throw new IllegalArgumentException("clazz is null");

        Path nativePath = NativeUtil.copyNativeFromJar(resourcePath, clazz);
        load(nativePath);
        return true;
    }
}
