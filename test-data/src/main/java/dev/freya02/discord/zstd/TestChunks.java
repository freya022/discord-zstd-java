package dev.freya02.discord.zstd;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AlgorithmParameters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestChunks {

    public static final Pattern compressedChunkPattern = Pattern.compile("Chunk-\\d+-\\d+\\.bin");
    public static final Pattern decompressedChunkPattern = Pattern.compile("Chunk-\\d+-\\d+\\.decompressed.bin");

    @Nonnull
    public static List<Chunk> get(@Nonnull Compression compression) {
        final EncryptedTestData encryptedTestData = getEncryptedTestData(compression);

        final List<Chunk> decryptedChunks = new ArrayList<>();

        try {
            final SecretKeySpec key = new SecretKeySpec(encryptedTestData.key, "AES");
            final AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(encryptedTestData.parameters);

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, parameters);

            for (Chunk chunk : encryptedTestData.chunks) {
                decryptedChunks.add(new Chunk(
                        cipher.doFinal(chunk.compressed),
                        cipher.doFinal(chunk.decompressed)
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return decryptedChunks;
    }

    @Nonnull
    private static EncryptedTestData getEncryptedTestData(@Nonnull Compression compression) {
        Path chunksDirectory = null;
        try {
            chunksDirectory = getChunksDirectory(compression);

            Base64.Decoder b64decoder = Base64.getDecoder();
            byte[] key = b64decoder.decode(
                    readEnvOrFile(compression.getKeyEnvVarName(), chunksDirectory.resolve("Key.bin"))
            );
            byte[] parameters = b64decoder.decode(
                    readEnvOrFile(compression.getParametersEnvVarName(), chunksDirectory.resolve("Parameters.bin"))
            );

            List<Chunk> chunks = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(chunksDirectory)) {
                for (Path path : stream.filter(TestChunks::isCompressedChunk).sorted().collect(Collectors.toList())) {
                    byte[] compressed = Files.readAllBytes(path);
                    Path decompressedPath = path.resolveSibling(path.getFileName().toString().replace(".bin", "") + ".decompressed.bin");
                    byte[] decompressed = Files.readAllBytes(decompressedPath);

                    chunks.add(new Chunk(compressed, decompressed));
                }
            }

            return new EncryptedTestData(key, parameters, chunks);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (chunksDirectory != null) {
                try {
                    chunksDirectory.getFileSystem().close();
                } catch (IOException e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        }
    }

    @Nonnull
    private static String readEnvOrFile(@Nonnull String varName, @Nonnull Path path) throws IOException {
        final String envVar = System.getenv(varName);
        if (envVar != null) return envVar;

        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static boolean isCompressedChunk(@Nonnull Path path) {
        return compressedChunkPattern.matcher(path.getFileName().toString()).matches();
    }

    @Nonnull
    private static Path getChunksDirectory(@Nonnull Compression compression) throws URISyntaxException, IOException {
        String resourcePath = compression.getResourcePath();
        URL resource = TestChunks.class.getResource(resourcePath);
        if (resource == null) {
            throw new AssertionError(String.format("No chunk directory was found at '%s'", resourcePath));
        }

        // Since we get consumed by other modules, this module will be a JAR
        @SuppressWarnings("resource") // The FileSystem will be closed after consumption
        FileSystem fileSystem = FileSystems.newFileSystem(resource.toURI(), Collections.emptyMap());
        return fileSystem.getPath(resourcePath);
    }

    public enum Compression {
        ZLIB,
        ZSTD,
        ;

        @Nonnull
        public String getResourcePath() {
            return "/chunks-" + name().toLowerCase();
        }

        @Nonnull
        public String getKeyEnvVarName() {
            return "TEST_DATA_" + name() + "_KEY";
        }

        @Nonnull
        public String getParametersEnvVarName() {
            return "TEST_DATA_" + name() + "_PARAMETERS";
        }
    }

    public static class Chunk {
        @Nonnull private final byte[] compressed;
        @Nonnull private final byte[] decompressed;

        private Chunk(@Nonnull byte[] compressed, @Nonnull byte[] decompressed) {
            this.compressed = compressed;
            this.decompressed = decompressed;
        }

        @Nonnull
        public byte[] getCompressed() {
            return compressed;
        }

        @Nonnull
        public byte[] getDecompressed() {
            return decompressed;
        }
    }

    private static class EncryptedTestData {
        @Nonnull private final byte[] key;
        @Nonnull private final byte[] parameters;
        @Nonnull private final List<Chunk> chunks;

        private EncryptedTestData(@Nonnull byte[] key, @Nonnull byte[] parameters, @Nonnull List<Chunk> chunks) {
            this.key = key;
            this.parameters = parameters;
            this.chunks = chunks;
        }
    }
}
