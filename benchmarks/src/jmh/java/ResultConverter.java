import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

private record BenchmarkResults(
        String benchmark,
        Metric primaryMetric,
        Map<String, Metric> secondaryMetrics
) {

    private record Metric(
            double score,
            String scoreError,
            String scoreUnit
    ) {

    }
}

void main(String[] args) throws IOException {
    var resultsJson = Files.readString(Path.of(args[0]));

    var resultsList = JsonMapper.shared().readValue(resultsJson, new TypeReference<List<BenchmarkResults>>() {
    });

    var zlibResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdDecompressorBenchmark.zlib")).findAny().orElseThrow();
    var zstdResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdDecompressorBenchmark.zstd")).findAny().orElseThrow();

    var zlibNoDeserResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdDecompressorBenchmark.zlibNoDeser")).findAny().orElseThrow();
    var zstdNoDeserResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdDecompressorBenchmark.zstdNoDeser")).findAny().orElseThrow();

    var zlibStreamResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdStreamingBenchmark.zlib")).findAny().orElseThrow();
    var zstdStreamResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdStreamingBenchmark.zstd")).findAny().orElseThrow();

    var zstdStreamNoDeserResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdStreamingBenchmark.zstdNoDeser")).findAny().orElseThrow();
    var zlibStreamNoDeserResults = resultsList.stream().filter(r -> r.benchmark.endsWith("ZstdStreamingBenchmark.zlibNoDeser")).findAny().orElseThrow();

    printDeserializationTable(zlibResults, zstdResults, zlibStreamResults, zstdStreamResults);
    printDecompressionTable(zlibNoDeserResults, zstdNoDeserResults, zlibStreamNoDeserResults, zstdStreamNoDeserResults);
}

private static void printDeserializationTable(BenchmarkResults zlibBulkResults, BenchmarkResults zstdBulkResults, BenchmarkResults zlibStreamResults, BenchmarkResults zstdStreamResults) {
    System.out.println("## Deserialization (input -> `DataObject`)");
    System.out.println("| Metric averages | Bulk Zlib (baseline) | Bulk Zstd | Streaming Zlib | Streaming Zstd |");
    System.out.println("|---------------------------|--------------------|-------------------|-------------------|-------------------|");
    System.out.printf("|Time per operation|%s|%s|%s|%s|\n", getScoreString(zlibBulkResults.primaryMetric, zlibBulkResults.primaryMetric), getScoreString(zstdBulkResults.primaryMetric, zlibBulkResults.primaryMetric), getScoreString(zlibStreamResults.primaryMetric, zlibBulkResults.primaryMetric), getScoreString(zstdStreamResults.primaryMetric, zlibBulkResults.primaryMetric));
    for (Map.Entry<String, BenchmarkResults.Metric> entry : zlibBulkResults.secondaryMetrics.entrySet()) {
        final String metricName = entry.getKey();
        final BenchmarkResults.Metric baselineMetric = entry.getValue();
        System.out.printf("|%s|%s|%s|%s|%s|\n", metricName, getScoreString(baselineMetric, baselineMetric), getScoreString(zstdBulkResults, metricName, baselineMetric), getScoreString(zlibStreamResults, metricName, baselineMetric), getScoreString(zstdStreamResults, metricName, baselineMetric));
    }
    System.out.println();
}

private static void printDecompressionTable(BenchmarkResults zlibBulkResults, BenchmarkResults zstdBulkResults, BenchmarkResults zlibStreamResults, BenchmarkResults zstdStreamResults) {
    System.out.println("## Decompression (decompresses and blackholes the data)");
    System.out.println("| Metric averages | Bulk Zlib (baseline) | Bulk Zstd | Streaming Zlib | Streaming Zstd |");
    System.out.println("|---------------------------|-------------------|-------------------|-------------------|-------------------|");
    System.out.printf("|Time per operation|%s|%s|%s|%s|\n", getScoreString(zlibBulkResults.primaryMetric, zlibBulkResults.primaryMetric), getScoreString(zstdBulkResults.primaryMetric, zlibBulkResults.primaryMetric), getScoreString(zlibStreamResults.primaryMetric, zlibBulkResults.primaryMetric), getScoreString(zstdStreamResults.primaryMetric, zlibBulkResults.primaryMetric));
    for (Map.Entry<String, BenchmarkResults.Metric> entry : zlibBulkResults.secondaryMetrics.entrySet()) {
        final String metricName = entry.getKey();
        final BenchmarkResults.Metric baselineMetric = entry.getValue();
        System.out.printf("|%s|%s|%s|%s|%s|\n", metricName, getScoreString(baselineMetric, baselineMetric), getScoreString(zstdBulkResults, metricName, baselineMetric), getScoreString(zlibStreamResults, metricName, baselineMetric), getScoreString(zstdStreamResults, metricName, baselineMetric));
    }
    System.out.println();
}

private static String getScoreString(BenchmarkResults results, String metricName, BenchmarkResults.Metric baselineMetric) {
    var metric = results.secondaryMetrics.get(metricName);
    return getScoreString(metric, baselineMetric);
}

private static String getScoreString(BenchmarkResults.Metric metric, BenchmarkResults.Metric baselineMetric) {
    if (metric == null) return "N/A";

    var baselineDiff = getBaselineDiffString(metric, baselineMetric);
    if ("NaN".equals(metric.scoreError)) {
        return "%.3f %s%s".formatted(metric.score, metric.scoreUnit, baselineDiff);
    } else {
        return "%.3f ± %.3f %s%s".formatted(metric.score, Double.valueOf(metric.scoreError), metric.scoreUnit, baselineDiff);
    }
}

private static String getBaselineDiffString(BenchmarkResults.Metric metric, BenchmarkResults.Metric baselineMetric) {
    var baselineDiff = -(100 - (100 / baselineMetric.score * metric.score));
    String signum;
    if (Math.signum(baselineDiff) == 0.0) {
        return "";
    } else if (Math.signum(baselineDiff) == 1.0) {
        signum = "+";
    } else {
        signum = "-";
    }
    return " (%s%.2f%%)".formatted(signum, Math.abs(baselineDiff));
}
