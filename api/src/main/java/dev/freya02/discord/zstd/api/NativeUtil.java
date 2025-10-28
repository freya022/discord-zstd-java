package dev.freya02.discord.zstd.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class NativeUtil {
    static String getCanonicalArchitecture(String arch) {
        arch = arch.toLowerCase().trim();
        if ("i386".equals(arch) || "i686".equals(arch)) {
            arch = "x86";
        } else if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            arch = "x86-64";
        }

        return arch;
    }

    static Path copyNativeFromJar(String resourcePath) throws IOException {
        final Path path = Files.createTempFile("libzstd", null);
        path.toFile().deleteOnExit();

        try (InputStream stream = NativeUtil.class.getResourceAsStream(resourcePath)) {
            if (stream == null)
                throw new FileNotFoundException("Natives not found at " + resourcePath);

            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return path;
    }
}
