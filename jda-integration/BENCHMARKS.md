# Benchmarks - Compared to JDA's Zlib decompression

## Overview

Zstd decompression is beneficial for larger bots where the CPU and GC allocations improvements will be the most noticeable, and for Discord itself. Smaller bots will only see slight benefits.

In both cases, payloads are slightly smaller mostly during startup, and quite similar during normal operation.

In the best case, when decompressing and deserializing, we see a 31% speed improvement with half the memory allocations.

## Synthetic benchmarks

> [!NOTE]
> Thanks to @MrPowerGamerBR for helping me gather some data from their bot and running benchmarks on their machine!

> [!TIP]
> "bulk" here means going from compressed `byte[]` to a fully decompressed `byte[]`.

With 10 shards starting up from scratch, giving us 364 MB of decompressed data, they ran [a few benchmarks](../benchmarks/src/jmh/java/dev/freya02/discord/zstd) comparing zlib, zstd, as well as their bulk (what JDA currently does) and stream (letting Jackson consume an `InputStream`) variants.

They were run on their production server, with an AMD Ryzen 5 5600X and produced [this data](https://gist.github.com/freya022/0516a809d43ee1d084ed205ac4fbe56c).

If we look at the complete package, transforming the compressed data into a usable `DataObject`, we can see bulk-decompressing with Zstd cut time spent by 30% and reduced GC allocations by 35%

However, if we use streaming, Zlib gets about 10% of speed increase and removes 48% of GC allocations, while Zstd gets a 31% speed improvement and the same memory improvements.

### Other benchmarks

I have also run some numbers on my machine (an AMD Ryzen 7 3700X, with boost disabled), not much different here:

- 10 shards of [randomly generated](../test-data-generator) data: https://gist.github.com/freya022/04d5e8cf7d44c9680ae42154808cddfd
- A bot with a single guild: https://gist.github.com/freya022/8922140965bc51a699b135ebc2f96914

## Runtime statistics

They were also kind enough to run their ~2.5K shards for a day while recording the compressed size, decompressed size and time-to-decompress.

The shards were split in 3 equally-sized sets:

- Using the current Zlib implementation
- Using Zstd with a 8 KB buffer
- Using Zstd with a 128 KB buffer

The results can be seen there: https://gist.github.com/freya022/7b35aa412a4f125ca1b139b71360ab45
