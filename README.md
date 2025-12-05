[api-maven-central-shield]: https://img.shields.io/maven-central/v/dev.freya02/discord-zstd-java-api?label=Maven%20central&logo=apachemaven
[api-maven-central-link]: https://central.sonatype.com/artifact/dev.freya02/discord-zstd-java-api
[jni-impl-maven-central-shield]: https://img.shields.io/maven-central/v/dev.freya02/discord-zstd-java-jni-impl?label=Maven%20central&logo=apachemaven
[jni-impl-maven-central-link]: https://central.sonatype.com/artifact/dev.freya02/discord-zstd-java-jni-impl

# discord-zstd-java

Modular support for Zstandard streaming decompression, for JVM Discord API wrappers.

## Supported platforms
- Linux: x86-64, aarch64, armhf
- Windows: x86-64, aarch64
- macOS (darwin): x86-64, aarch64

## 🤖 For bot developers

### Installation

You're likely here if you want to use Zstd decompression for your Discord bot!

[![discord-zstd-java-jni-impl on Maven Central][jni-impl-maven-central-shield] ][jni-impl-maven-central-link]

This is compatible with Java 8+.

#### Gradle
```kotlin
dependencies {
    runtimeOnly("dev.freya02:discord-zstd-java-jni-impl:VERSION") // TODO replace VERSION with current release
}
```

#### Maven
```xml
<dependency>
    <groupId>dev.freya02</groupId>
    <artifactId>discord-zstd-java-jni-impl</artifactId>
    <version>VERSION</version> <!-- TODO replace VERSION with current release -->
    <scope>runtime</scope>
</dependency>
```

### Usage
As a bot developer, you don't need to do anything.

If you want to load a different version of the native library,
you can do so by calling `ZstdNativesLoader.load(Path)` or `loadFromJar(String)`. These functions will return `false` if the natives were already loaded, as they can't be replaced.

## 📖 For library developers
### Installation

[![discord-zstd-java-api on Maven Central][api-maven-central-shield] ][api-maven-central-link]

You will only need the `dev.freya02:discord-zstd-java-api:VERSION` dependency, it is compatible with Java 8+.

### Usage

The main interface is `DiscordZstd`, you can get an instance with `DiscordZstdProvider.get()`.
Then, you can either:
1. Do bulk processing with a decompressor obtained with `DiscordZstd#createDecompressor` and kept per gateway connection,
   calling `ZstdDecompressor#decompress` on each gateway message
2. Process gradually with a context obtained from `DiscordZstd#createContext` and kept per gateway connection,
    then making input streams with `ZstdContext#createInputStream` from each gateway message
