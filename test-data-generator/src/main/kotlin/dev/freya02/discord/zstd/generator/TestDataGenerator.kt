package dev.freya02.discord.zstd.generator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.internal.utils.compress.ZlibDecompressor
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.Deflater
import kotlin.concurrent.withLock
import kotlin.io.path.*
import kotlin.random.Random

suspend fun main(args: Array<String>) {
    require(args.size == 1) {
        error("An argument is required for the output directory")
    }

    val path = Path(args[0])
    if (path.exists() && path.walk().any { it != path }) {
        println("${path.absolutePathString()} already exists, overwrite? (yes/other)")
        val line = readln()
        if (line.lowercase() != "yes" && line.lowercase() != "y") return
    }

    val start = System.nanoTime()

    val baseMessages = listOf(
        """{"t":null,"s":null,"op":10,"d":{"heartbeat_interval":41250,"_trace":["[\"gateway-prd-arm-us-east1-c-rzdj\",{\"micros\":0.0}]"]}}""",
        """{"t":null,"s":null,"op":11,"d":null}""",
        """{"t":"READY","s":1,"op":0,"d":{"v":10,"user_settings":{},"user":{"verified":true,"username":"Test","primary_guild":null,"mfa_enabled":true,"id":"1896186316901649619","global_name":null,"flags":0,"email":null,"discriminator":"8597","clan":null,"bot":true,"avatar":null},"shard":[0,1],"session_type":"normal","session_id":"46518a1bf6912ed649618c831861ab91","resume_gateway_url":"wss://gateway-us-east1-c.discord.gg","relationships":[],"private_channels":[],"presences":[],"guilds":[{"unavailable":true,"id":"961969618123483218"}],"guild_join_requests":[],"geo_ordered_rtc_regions":["frankfurt","milan","paris","rotterdam","london"],"game_relationships":[],"auth":{},"application":{"id":"1896186316901649619","flags":41975808},"_trace":["[\"gateway-prd-arm-us-east1-c-rzdj\",{\"micros\":111772,\"calls\":[\"id_created\",{\"micros\":566,\"calls\":[]},\"session_lookup_time\",{\"micros\":4822,\"calls\":[]},\"session_lookup_finished\",{\"micros\":19,\"calls\":[]},\"discord-sessions-prd-2-74\",{\"micros\":105284,\"calls\":[\"start_session\",{\"micros\":103718,\"calls\":[\"discord-api-rpc-587fd65c9b-ktqwd\",{\"micros\":48220,\"calls\":[\"get_user\",{\"micros\":5425},\"get_guilds\",{\"micros\":5757},\"send_scheduled_deletion_message\",{\"micros\":15},\"guild_join_requests\",{\"micros\":10},\"authorized_ip_coro\",{\"micros\":10},\"pending_payments\",{\"micros\":42193},\"apex_experiments\",{\"micros\":6130},\"user_activities\",{\"micros\":4},\"played_application_ids\",{\"micros\":2},\"linked_users\",{\"micros\":2}]}]},\"starting_guild_connect\",{\"micros\":34,\"calls\":[]},\"presence_started\",{\"micros\":314,\"calls\":[]},\"guilds_started\",{\"micros\":60,\"calls\":[]},\"lobbies_started\",{\"micros\":1,\"calls\":[]},\"guilds_connect\",{\"micros\":1,\"calls\":[]},\"presence_connect\",{\"micros\":1098,\"calls\":[]},\"connect_finished\",{\"micros\":1107,\"calls\":[]},\"build_ready\",{\"micros\":13,\"calls\":[]},\"clean_ready\",{\"micros\":1,\"calls\":[]},\"optimize_ready\",{\"micros\":1,\"calls\":[]},\"split_ready\",{\"micros\":0,\"calls\":[]}]}]}]"]}}""",
    )

    val mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()

    val shards = arrayListOf<List<GuildCreate>>()
    val sizes = arrayListOf<Int>()

    val random = Random(Random.nextLong())
    val nShards = 10
    val nGuilds = random.nextInt(1500, 1900)

    coroutineScope {
        val context = Dispatchers.IO.limitedParallelism(12)
        repeat(nShards) {
            val lock = ReentrantLock()
            var sequence = 2
            val guildCreates = arrayListOf<GuildCreate>()
            shards += guildCreates
            repeat(nGuilds) {
                launch(context) {
                    val guildCreate = context(random) { GuildCreateGenerator.generate(sequence++) }
                    val asBytes = mapper.writeValueAsBytes(guildCreate)

                    lock.withLock {
                        guildCreates += guildCreate
                        sizes += asBytes.size
                    }
                }
            }
        }
    }

    val end = System.nanoTime()

    val totalGeneratedSize = sizes.sumOf { it.toLong() }
    println("Generated ${shards.sumOf { it.size }} guild creates in ${totalGeneratedSize / 1024.0 / 1024.0 } MB of data in ${(end - start) / 1000000000.0} s")

    val start2 = System.nanoTime()

    coroutineScope {
        val context = Dispatchers.IO.limitedParallelism(12)
        shards.forEachIndexed { shardIndex, shard ->
            launch(context) {
                val shardFolder = path.resolve("shard-$shardIndex").createDirectories()
                val zlibDecompressor = ZlibDecompressor(2048)
                Deflater(Deflater.DEFAULT_COMPRESSION).use { compressor ->
                    var chunkIndex = 0
                    for (baseMessage in baseMessages) {
                        val chunkPath = shardFolder.resolve("chunk-${chunkIndex++}.bin.zlib")
                        val messageBytes = baseMessage.encodeToByteArray()

                        chunkPath.writeCompressed(compressor, zlibDecompressor, messageBytes)
                    }
                    for (guildCreate in shard) {
                        val chunkPath = shardFolder.resolve("chunk-${chunkIndex++}.bin.zlib")
                        val messageBytes = mapper.writeValueAsBytes(guildCreate)

                        chunkPath.writeCompressed(compressor, zlibDecompressor, messageBytes)
                    }
                }
            }
        }
    }

    val end2 = System.nanoTime()
    println("Compressed in ${(end2 - start2) / 1000000.0} ms")
}

private fun Path.writeCompressed(
    compressor: Deflater,
    zlibDecompressor: ZlibDecompressor,
    messageBytes: ByteArray,
) {
    val bytes = ByteArray(32768)
    compressor.setInput(messageBytes)
    outputStream().use { out ->
        while (!compressor.needsInput()) {
            val compressed = compressor.deflate(bytes, 0, bytes.size, Deflater.SYNC_FLUSH)
            out.write(bytes, 0, compressed)
        }
    }

    val decompressed = zlibDecompressor.decompress(readBytes())
    require(decompressed.contentEquals(messageBytes))
}
