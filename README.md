# discord-zstd-java

Modular support for Zstandard streaming decompression, for JVM Discord API wrappers.

## Supported platforms
- Linux: x86-64, aarch64, armhf
- Windows: x86-64, aarch64
- macOS (darwin): x86-64, aarch64

## 🤖 For bot developers

### Installation

You're likely here if you want to use Zstd decompression for your Discord bot! You can choose between two different implementations:

#### For Java 22+ (Recommended)

For improved performance, you can use this implementation based on the [Foreign Function & Memory API](https://openjdk.org/jeps/454).

##### Gradle
```kotlin
dependencies {
    runtimeOnly("dev.freya02:discord-zstd-java-ffm-impl:VERSION") // TODO replace VERSION with current release
}
```

##### Maven
```xml
<dependency>
    <groupId>dev.freya02</groupId>
    <artifactId>discord-zstd-java-ffm-impl</artifactId>
    <version>VERSION</version> <!-- TODO replace VERSION with current release -->
    <scope>runtime</scope>
</dependency>
```

#### For Java 8+
For maximum compatibility, you can use this implementation based on [JNA](https://github.com/java-native-access/jna).

##### Gradle
```kotlin
dependencies {
    runtimeOnly("dev.freya02:discord-zstd-java-jna-impl:VERSION") // TODO replace VERSION with current release
}
```

##### Maven
```xml
<dependency>
    <groupId>dev.freya02</groupId>
    <artifactId>discord-zstd-java-jna-impl</artifactId>
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

You will only need the `dev.freya02:discord-zstd-java-api:VERSION` dependency, it is compatible with Java 8+.

### Usage

You can check if the natives are loaded by checking `ZstdNativesLoader.isLoaded()`,
but usually you'll want to call `loadFromJar()`, you should do it late enough so the bot developer has a chance to load different natives.

You can get a `ZstdDecompressorFactory` by loading one using a `ServiceLoader`.
