package dev.freya02.discord.zstd;

import org.jspecify.annotations.NullMarked;

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

@NullMarked
public class TestChunks {

    public static final Pattern compressedChunkPattern = Pattern.compile("Chunk-\\d+-\\d+\\.bin");
    public static final Pattern decompressedChunkPattern = Pattern.compile("Chunk-\\d+-\\d+\\.decompressed.bin");

    public static List<Chunk> get(Compression compression) {
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

    private static EncryptedTestData getEncryptedTestData(Compression compression) {
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

    private static String readEnvOrFile(String varName, Path path) throws IOException {
        final String envVar = System.getenv(varName);
        if (envVar != null) return envVar;

        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Env var '" + varName + "' is not present", e);
        }
    }

    private static boolean isCompressedChunk(Path path) {
        return compressedChunkPattern.matcher(path.getFileName().toString()).matches();
    }

    private static Path getChunksDirectory(Compression compression) throws URISyntaxException, IOException {
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

        public String getResourcePath() {
            return "/chunks-" + name().toLowerCase();
        }

        public String getKeyEnvVarName() {
            return "TEST_DATA_" + name() + "_KEY";
        }

        public String getParametersEnvVarName() {
            return "TEST_DATA_" + name() + "_PARAMETERS";
        }
    }

    public static class Chunk {
        private final byte[] compressed;
        private final byte[] decompressed;

        private Chunk(byte[] compressed, byte[] decompressed) {
            this.compressed = compressed;
            this.decompressed = decompressed;
        }

        public byte[] getCompressed() {
            return compressed;
        }

        public byte[] getDecompressed() {
            return decompressed;
        }
    }

    private static class EncryptedTestData {
        private final byte[] key;
        private final byte[] parameters;
        private final List<Chunk> chunks;

        private EncryptedTestData(byte[] key, byte[] parameters, List<Chunk> chunks) {
            this.key = key;
            this.parameters = parameters;
            this.chunks = chunks;
        }
    }
}
