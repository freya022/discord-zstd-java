[api-maven-central-shield]: https://img.shields.io/maven-central/v/dev.freya02/discord-zstd-java-api?label=Maven%20central&logo=apachemaven
[api-maven-central-link]: https://central.sonatype.com/artifact/dev.freya02/discord-zstd-java-api
[jni-impl-maven-central-shield]: https://img.shields.io/maven-central/v/dev.freya02/discord-zstd-java-jni-impl?label=Maven%20central&logo=apachemaven
[jni-impl-maven-central-link]: https://central.sonatype.com/artifact/dev.freya02/discord-zstd-java-jni-impl

# discord-zstd-java

Lightweight modular support for Zstandard streaming decompression, for JVM Discord API wrappers.

## Supported platforms
- Linux: x86-64, aarch64, armhf
- Windows: x86-64, aarch64
- macOS (darwin): x86-64, aarch64

## 🔥 For JDA users

See the [JDA integration module](jda-integration).

## 📖 For library developers

[![discord-zstd-java-api on Maven Central][api-maven-central-shield] ][api-maven-central-link]

### Built-in integration

If you decide to integrate this library into yours,
you will only need the `dev.freya02:discord-zstd-java-api:VERSION` dependency, it is compatible with Java 8+.

Your users will need to install an implementation, we recommend using `discord-zstd-java-jni-impl`.

The users can also load a different version of the native library,
they can do so by calling `ZstdNativesLoader.load(Path)` or `loadFromJar(String)`.
These functions will return `false` if the natives were already loaded, as they can't be replaced.

#### Usage

The main interface is `DiscordZstd`, you can get an instance with `DiscordZstdProvider.get()`.
Then, you can either:
1. Do bulk processing with a decompressor obtained with `DiscordZstd#createDecompressor` and kept per gateway connection,
   calling `ZstdDecompressor#decompress` on each gateway message
2. Process gradually with a context obtained from `DiscordZstd#createContext` and kept per gateway connection,
   then making input streams with `ZstdContext#createInputStream` from each gateway message

### External integration

You can also provide an API in your library, this way users can choose to use any decompression library they want.
After creating your API, please submit a pull request here with a new module which implements that API.
