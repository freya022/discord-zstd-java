package dev.freya02.discord.zstd.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class IOUtil {
    static Path copyNativeFromJar(String resourcePath, Class<?> clazz) throws IOException {
        final Path path = Files.createTempFile("libzstd", null);
        path.toFile().deleteOnExit();

        try (InputStream stream = clazz.getResourceAsStream(resourcePath)) {
            if (stream == null)
                throw new FileNotFoundException("Natives not found at '" + resourcePath + "' relative to '" + clazz.getPackage().getName() + "'");

            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return path;
    }
}
