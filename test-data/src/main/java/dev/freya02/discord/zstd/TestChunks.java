package dev.freya02.discord.zstd;

import org.jspecify.annotations.NullMarked;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
import java.util.stream.Stream;

@NullMarked
public class TestChunks {

    public static final Pattern shardFolderPattern = Pattern.compile("shard-\\d+");
    public static final Pattern decompressedChunkPattern = Pattern.compile("chunk-\\d+\\.bin");

    public static List<List<Chunk>> get() {
        final EncryptedTestData encryptedTestData = getEncryptedTestData();

        final List<List<Chunk>> decryptedShards = new ArrayList<>();

        try {
            final SecretKeySpec key = new SecretKeySpec(encryptedTestData.key, "AES");
            final AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(encryptedTestData.parameters);

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, parameters);

            for (List<Chunk> shard : encryptedTestData.shards) {
                List<Chunk> decryptedChunks = new ArrayList<>();
                for (Chunk chunk : shard) {
                    decryptedChunks.add(new Chunk(
                            cipher.doFinal(chunk.decompressed),
                            cipher.doFinal(chunk.zlibCompressed),
                            cipher.doFinal(chunk.zstdCompressed)
                    ));
                }
                decryptedShards.add(decryptedChunks);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return decryptedShards;
    }

    private static EncryptedTestData getEncryptedTestData() {
        Path chunksDirectory = null;
        try {
            chunksDirectory = getChunksDirectory();

            Base64.Decoder b64decoder = Base64.getDecoder();
            byte[] key = b64decoder.decode(
                    readEnvOrFile("TEST_DATA_KEY", chunksDirectory.resolve("Key.bin"))
            );
            byte[] parameters = b64decoder.decode(
                    readEnvOrFile("TEST_DATA_PARAMETERS", chunksDirectory.resolve("Parameters.bin"))
            );

            List<List<Chunk>> shards = new ArrayList<>();
            try (Stream<Path> shardDirStream = Files.walk(chunksDirectory, 1)) {
                for (Path shardDir : shardDirStream.filter(TestChunks::isShardFolder).sorted().toList()) {
                    List<Chunk> chunks = new ArrayList<>();
                    try (Stream<Path> chunkStream = Files.walk(shardDir, 1)) {
                        for (Path path : chunkStream.filter(TestChunks::isDecompressedChunk).sorted().toList()) {
                            byte[] decompressed = Files.readAllBytes(path);
                            byte[] zlibCompressed = Files.readAllBytes(path.resolveSibling(path.getFileName().toString() + ".zlib"));
                            byte[] zstdCompressed = Files.readAllBytes(path.resolveSibling(path.getFileName().toString() + ".zstd"));

                            chunks.add(new Chunk(decompressed, zlibCompressed, zstdCompressed));
                        }
                    }
                    shards.add(chunks);
                }
            }

            return new EncryptedTestData(key, parameters, shards);
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
            return Files.readString(path);
        } catch (IOException e) {
            throw new IOException("Env var '" + varName + "' is not present", e);
        }
    }

    private static boolean isShardFolder(Path path) {
        return shardFolderPattern.matcher(path.getFileName().toString()).matches();
    }

    private static boolean isDecompressedChunk(Path path) {
        return decompressedChunkPattern.matcher(path.getFileName().toString()).matches();
    }

    private static Path getChunksDirectory() throws URISyntaxException, IOException {
        String resourcePath = "/chunks";
        URL resource = TestChunks.class.getResource(resourcePath);
        if (resource == null) {
            throw new AssertionError(String.format("No chunk directory was found at '%s'", resourcePath));
        }

        // Since we get consumed by other modules, this module will be a JAR
        @SuppressWarnings("resource") // The FileSystem will be closed after consumption
        FileSystem fileSystem = FileSystems.newFileSystem(resource.toURI(), Collections.emptyMap());
        return fileSystem.getPath(resourcePath);
    }

    public record Chunk(byte[] decompressed, byte[] zlibCompressed, byte[] zstdCompressed) {
    }

    private record EncryptedTestData(byte[] key, byte[] parameters, List<List<Chunk>> shards) {
    }
}
