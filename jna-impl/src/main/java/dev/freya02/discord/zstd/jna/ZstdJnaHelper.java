package dev.freya02.discord.zstd.jna;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class ZstdJnaHelper {
    public static ZstdJna load() {
        String lib = System.getProperty("zstd.lib");
        // On Windows and macOS, if lib is null, it will link functions from the current process,
        // on Linux, `System.load` does not allow JNA to find the functions, see https://github.com/java-native-access/jna/issues/1698
        if (lib == null && Platform.isLinux()) {
            throw new IllegalStateException("Cannot load Zstd from the current process on Linux systems, 'zstd.lib' must be set before ZstdJna is used");
        }

        return Native.load(lib, ZstdJna.class);
    }
}
