[api-maven-central-shield]: https://img.shields.io/maven-central/v/dev.freya02/discord-zstd-java-api?label=Maven%20central&logo=apachemaven
[api-maven-central-link]: https://central.sonatype.com/artifact/dev.freya02/discord-zstd-java-api
[jda-integration-maven-central-shield]: https://img.shields.io/maven-central/v/dev.freya02/discord-zstd-java-jda-integration?label=Maven%20central&logo=apachemaven
[jda-integration-maven-central-link]: https://central.sonatype.com/artifact/dev.freya02/discord-zstd-java-jda-integration

# discord-zstd-java - JDA integration

Lightweight Zstandard decompression for the Java Discord API. (JDA)

## Installation

[![discord-zstd-java-jda-integration on Maven Central][jda-integration-maven-central-shield] ][jda-integration-maven-central-link]

This is compatible with Java 8+.

### Gradle
```kotlin
dependencies {
    implementation("dev.freya02:discord-zstd-java-jda-integration:VERSION") // TODO replace VERSION with current release
}
```

### Maven
```xml
<dependency>
    <groupId>dev.freya02</groupId>
    <artifactId>discord-zstd-java-jda-integration</artifactId>
    <version>VERSION</version> <!-- TODO replace VERSION with current release -->
</dependency>
```

> [!TIP]
> To remove the warning when the natives are loaded, add `--enable-native-access=ALL-UNNAMED` to your JVM arguments.

## Usage

JDA's gateway decompressor can be configured in `GatewayConfig.Builder`, in two ways:

- `useBufferedTransportDecompression` lets you use decompress payloads all at once, this is what JDA does by default.
- `useStreamedTransportDecompression` lets you decompress payloads progressively, this means no memory allocations for decompression, but also prevents from printing corrupted payloads (though extremely rare)

If you choose to use buffered decompression, use `ZstdBufferedTransportGatewayDecompressor`, if you want to use streamed decompression, use `ZstdStreamedTransportGatewayDecompressor`.

For example:

```java
void main(String[] args) {
    DefaultShardManagerBuilder
            .createDefault(args[0])
            .setGatewayConfig(
                    GatewayConfig.builder()
                            .useStreamedTransportDecompression(ZstdStreamedTransportGatewayDecompressor.supplier())
                            .build())
            .build();
}
```

## Overriding natives

If you want to load a different version of the native library,
you can do so by calling `ZstdNativesLoader.load(Path)` or `loadFromJar(String)`. These functions will return `false` if the natives were already loaded, as they can't be replaced.
