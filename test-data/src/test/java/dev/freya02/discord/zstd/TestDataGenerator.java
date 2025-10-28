package dev.freya02.discord.zstd;

import javax.crypto.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class TestDataGenerator {
    /**
     * This program encrypts compressed messages from Discord's gateway.
     *
     * <p>Create a directory named {@code chunks-zlib} or {@code chunks-zstd} depending on the compression used,
     * which is set on the first lines,
     * then put each gateway message in their own file,
     * following the pattern {@code Chunk-[shard number]-[per shard chunk number].bin}.
     */
    public static void main(String[] args) throws Exception {
        // Input parameters
        TestChunks.Compression compression = TestChunks.Compression.ZLIB;

        // DO NOT CHANGE BELOW
        String folderName = "chunks-" + compression.name().toLowerCase();

        Path cleartextPath = Paths.get(folderName);
        if (Files.notExists(cleartextPath)) {
            throw new IllegalArgumentException(cleartextPath.toAbsolutePath() + " does not exist");
        }
        Path encryptedPath = Files.createDirectories(Paths.get(folderName + "-encrypted"));

        Encryptor encryptor = new Encryptor();
        for (Path path : getCleartextFiles(cleartextPath)) {
            byte[] ciphertext = encryptor.encryptFrom(path);
            Files.write(encryptedPath.resolve(path.getFileName()), ciphertext, CREATE, TRUNCATE_EXISTING);
        }

        Files.write(encryptedPath.resolve("Key.bin"), encryptor.getBase64Key(), CREATE, TRUNCATE_EXISTING);
        Files.write(encryptedPath.resolve("Parameters.bin"), encryptor.getBase64Parameters(), CREATE, TRUNCATE_EXISTING);

        System.out.printf("Created encrypted chunks, cipher key and parameters @ %s\n", encryptedPath.toAbsolutePath());
        System.out.printf("You can replace the existing test files in the `src/main/resources%s` directory.\n", compression.getResourcePath());
        System.out.printf("The key and parameters can be added to the environment variables (%s and %s), in which case you don't need the files\n", compression.getKeyEnvVarName(), compression.getParametersEnvVarName());
    }

    private static List<Path> getCleartextFiles(Path cleartextPath) throws IOException {
        try (Stream<Path> walk = Files.walk(cleartextPath)) {
            return walk.filter(TestDataGenerator::isChunk).collect(Collectors.toList());
        }
    }

    private static boolean isChunk(Path path) {
        final String fileName = path.getFileName().toString();
        return TestChunks.decompressedChunkPattern.matcher(fileName).matches()
                || TestChunks.compressedChunkPattern.matcher(fileName).matches();
    }

    private static class Encryptor {
        private final SecretKey key;
        private final Cipher cipher;

        public Encryptor() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
            key = KeyGenerator.getInstance("AES").generateKey();

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
        }

        public byte[] encryptFrom(Path path) throws IllegalBlockSizeException, BadPaddingException, IOException {
            return cipher.doFinal(Files.readAllBytes(path));
        }

        public byte[] getBase64Key() {
            return Base64.getEncoder().encode(key.getEncoded());
        }

        public byte[] getBase64Parameters() throws IOException {
            return Base64.getEncoder().encode(cipher.getParameters().getEncoded());
        }
    }
}
