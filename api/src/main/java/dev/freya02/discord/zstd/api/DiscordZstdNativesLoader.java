package dev.freya02.discord.zstd.api;

import dev.freya02.discord.zstd.internal.Checks;
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
public class DiscordZstdNativesLoader {
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
        Checks.notNull(path, "Path");
        if (!path.isAbsolute())
            throw new IllegalArgumentException("path is not absolute: " + path);

        final String pathStr = path.toAbsolutePath().toString();
        System.setProperty("zstd.lib", pathStr);
        System.load(pathStr);
        init = true;
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
        Checks.notNull(resourcePath, "Resource path");
        Checks.notNull(clazz, "Class");

        Path nativePath = IOUtil.copyNativeFromJar(resourcePath, clazz);
        load(nativePath);
        return true;
    }

    /**
     * Loads the natives using the provided path from the provided class.
     *
     * <p>When, and only when using modules, the resource will be loaded from one of the modules loaded by the provided class loader.
     * <br>If the resource path does not represent a valid package name, it can be loaded from any module, if it forms a valid package name,
     * it is subject to encapsulation rules specified by {@link java.lang.Module#getResourceAsStream(String) Module.getResourceAsStream}.
     *
     * @param  resourcePath
     *         The path to the native library in the provided class
     * @param  loader
     *         The class loader from which to load the resource from
     *
     * @throws IllegalArgumentException
     *         If {@code resourcePath} or {@code loader} is {@code null}
     * @throws IOException
     *         If the resource does not exist or when extracting it fails
     *
     * @return {@code true} if the natives were loaded, {@code false} if they already were
     */
    public static synchronized boolean loadFromJar(String resourcePath, ClassLoader loader) throws IOException {
        if (init)
            return false;
        Checks.notNull(resourcePath, "Resource path");
        Checks.notNull(loader, "Class loader");

        Path nativePath = IOUtil.copyNativeFromJar(resourcePath, loader);
        load(nativePath);
        return true;
    }
}
