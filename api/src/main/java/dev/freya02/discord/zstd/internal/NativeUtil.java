package dev.freya02.discord.zstd.internal;

public class NativeUtil {
    public static System getSystem() {
        String architecture = getCanonicalArchitecture(java.lang.System.getProperty("os.arch"));
        String osName = java.lang.System.getProperty("os.name");

        String platform;
        String sharedLibraryExtension;
        if (osName.startsWith("Linux")) {
            if (!architecture.equals("x86-64") && !architecture.equals("aarch64") && !architecture.equals("arm"))
                throw new IllegalStateException("Unsupported architecture: " + architecture);
            platform = "linux-" + architecture;
            sharedLibraryExtension = "so";
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            platform = "darwin";
            sharedLibraryExtension = "dylib";
        } else if (osName.startsWith("Windows")) {
            if (!architecture.equals("x86-64") && !architecture.equals("aarch64"))
                throw new IllegalStateException("Unsupported architecture: " + architecture);
            platform = "win32-" + architecture;
            sharedLibraryExtension = "dll";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        return new System(platform, sharedLibraryExtension);
    }

    private static String getCanonicalArchitecture(String arch) {
        arch = arch.toLowerCase().trim();
        if ("i386".equals(arch) || "i686".equals(arch)) {
            arch = "x86";
        } else if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            arch = "x86-64";
        }

        return arch;
    }

    public static class System {
        public final String platform;
        public final String sharedLibraryExtension;

        private System(String platform, String sharedLibraryExtension) {
            this.platform = platform;
            this.sharedLibraryExtension = sharedLibraryExtension;
        }
    }
}
