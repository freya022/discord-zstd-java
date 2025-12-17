package dev.freya02.discord.zstd.generator

import dev.freya02.discord.zstd.generator.random.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.ChannelType.*
import net.dv8tion.jda.api.interactions.DiscordLocale
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

object GuildCreateGenerator {
    private val features = "ANIMATED_BANNER,ANIMATED_ICON,APPLICATION_COMMAND_PERMISSIONS_V2,AUTO_MODERATION,BANNER,COMMUNITY,CREATOR_MONETIZABLE_PROVISIONAL,CREATOR_STORE_PAGE,DEVELOPER_SUPPORT_SERVER,DISCOVERABLE,FEATURABLE,INVITES_DISABLED,INVITE_SPLASH,MEMBER_VERIFICATION_GATE_ENABLED,MORE_SOUNDBOARD,MORE_STICKERS,NEWS,PARTNERED,PREVIEW_ENABLED,RAID_ALERTS_DISABLED,ROLE_ICONS,ROLE_SUBSCRIPTIONS_AVAILABLE_FOR_PURCHASE,ROLE_SUBSCRIPTIONS_ENABLED,SOUNDBOARD,TICKETED_EVENTS_ENABLED,VANITY_URL,VERIFIED,VIP_REGIONS,WELCOME_SCREEN_ENABLED,GUESTS_ENABLED,GUILD_TAGS,ENHANCED_ROLE_COLORS"
        .split(",")

    private val words = run {
        val clazz = GuildCreateGenerator::class.java
        val basicEnglish = clazz.getResourceAsStream("/basic_english_2000.txt")!!.readBytes().decodeToString().lines()
        val simplifiedEnglish = clazz.getResourceAsStream("/simplified_english.txt")!!.readBytes().decodeToString().lines()

        basicEnglish + simplifiedEnglish
    }

    context(random: Random)
    private fun createEmoji(roles: List<GuildCreate.Data.Role>) = GuildCreate.Data.Emoji(
        version = random.nextVersion(),
        roles = roles.takeRandomly(random, 0..min(roles.size, 5)) { it.id },
        requiresColons = random.nextBoolean(),
        name = words.random(random),
        managed = random.nextBoolean(),
        id = random.nextSnowflake(),
        available = random.nextBoolean(),
        animated = random.nextBoolean(),
    )

    context(random: Random)
    private fun createRole(premiumTier: Int, hasStyledRoles: Boolean) = GuildCreate.Data.Role(
        version = random.nextVersion(),
        unicodeEmoji = random.nextUnicodeEmoji(),
        tags = GuildCreate.Data.Role.Tags(
            botId = random.takeRandomly { nextSnowflake() },
        ),
        position = random.nextInt(0, 50),
        permissions = random.nextPermissions(),
        name = random.nextString(alphanumericChars, 3..32),
        mentionable = random.nextBoolean(),
        managed = random.nextBoolean(),
        id = random.nextSnowflake(),
        icon = if (premiumTier >= 2) random.takeRandomly { nextHash() } else null,
        hoist = random.nextBoolean(),
        flags = random.nextInt(0..1),
        colors = GuildCreate.Data.Role.Colors(
            primaryColor = random.nextInt(0x000000..0xFFFFFF),
            secondaryColor = if (hasStyledRoles) random.nextInt(0x000000..0xFFFFFF) else 0,
            tertiaryColor = if (hasStyledRoles) random.nextInt(0x000000..0xFFFFFF) else 0,
        )
    )

    context(random: Random)
    private fun createPermissionOverwrite(roles: MutableList<GuildCreate.Data.Role>, userIds: MutableList<Snowflake>): GuildCreate.Data.Channel.Overwrite {
        val type = if (roles.isEmpty()) 1 else random.nextInt(0, 1)
        val id = when (type) {
            0 -> roles.removeAt(roles.indices.random(random)).id
            1 -> userIds.removeAt(userIds.indices.random(random))
            else -> throw AssertionError()
        }

        return GuildCreate.Data.Channel.Overwrite(id, type, random.nextPermissions(), random.nextPermissions())
    }

    context(random: Random)
    private fun createChannel(isThread: Boolean, roles: List<GuildCreate.Data.Role>, userIds: List<Snowflake>): GuildCreate.Data.Channel {
        val type = if (isThread) random.nextInt(11..12) else random.nextInt(0..16)
        val hasTopic = type.let(ChannelType::fromId).let { type ->
            when (type) {
                TEXT, NEWS, FORUM, MEDIA -> true
                else -> false
            }
        }

        val permissionOverrides = run {
            val mutableUserIds = userIds.toMutableList()
            val mutableRoles = roles.toMutableList()

            val nMax = 10.coerceAtMost(roles.size + userIds.size)
            random.nextList(0..nMax) { createPermissionOverwrite(mutableRoles, mutableUserIds) }
        }

        return GuildCreate.Data.Channel(
            id = random.nextSnowflake(),
            type = type,
            version = random.nextVersion(),
            position = random.nextInt(0..50),
            permissionOverwrites = permissionOverrides,
            name = words.random(random),
            topic = if (hasTopic) random.nextString(words, random.fromDistribution(20 to 0..0, 30 to 1..80, 20 to 80..256, 20 to 256..512, 10 to 512..1024)) else null,
            nsfw = random.nextBoolean(),
            lastMessageId = random.nextSnowflake(),
            lastPinTimestamp = random.nextInstant(),
            parentId = null,
            flags = 1 shl random.nextInt(1..15),
            defaultAutoArchiveDuration = random.nextInt(),
        )
    }

    context(random: Random)
    private fun createSticker(guildId: Snowflake) = GuildCreate.Data.Sticker(
        version = random.nextVersion(),
        type = random.nextInt(1..2),
        tags = random.nextList(1..5) { words.random(random) }.joinToString(","),
        name = words.random(random),
        id = random.nextSnowflake(),
        guildId = guildId,
        formatType = random.nextInt(1, 4),
        description = random.nextString(words, 3..100),
        available = random.nextBoolean()
    )

    context(random: Random)
    private fun createScheduledEvent(guildId: Snowflake, userIds: List<Snowflake>, channels: List<GuildCreate.Data.Channel>) = GuildCreate.Data.ScheduledEvent(
        id = random.nextSnowflake(),
        guildId = guildId,
        channelId = random.takeRandomly { channels.random(this).id },
        creatorId = userIds.random(random),
        name = random.nextString(alphanumericChars, 3..32),
        description = random.takeRandomly { random.nextString(words, 0..200) },
        scheduledStartTime = random.nextInstant(),
        scheduledEndTime = random.takeRandomly { nextInstant() }
    )

    context(random: Random)
    private fun createMember(userId: Snowflake, roles: List<GuildCreate.Data.Role>): GuildCreate.Data.Member {
        val roleAmount = random.fromDistribution(80 to 0..<5, 15 to 5..<10, 5 to 10..20)

        return GuildCreate.Data.Member(
            user = GuildCreate.Data.User(
                username = random.nextString(alphanumericChars, 3..32),
                publicFlags = User.UserFlag.getRaw(random.nextSet(0..User.UserFlag.entries.size) { random.nextEntry<User.UserFlag>() }),
                primaryGuild = random.takeRandomly {
                    GuildCreate.Data.User.PrimaryGuild(
                        nextSnowflake(),
                        nextBoolean(),
                        nextString(letterChars, 2..4),
                        nextHash()
                    )
                },
                id = userId,
                globalName = random.takeRandomly { nextString(alphanumericChars, 3..32) },
                displayName = random.takeRandomly { nextString(alphanumericChars, 3..32) },
                discriminator = random.nextString(digitChars, 4..4),
                collectibles = GuildCreate.Data.User.Collectibles(random.takeRandomly {
                    GuildCreate.Data.User.Collectibles.Nameplate(
                        skuId = nextSnowflake(),
                        asset = nextHash(), /* Not a hash but w/e */
                        label = nextString(words, 4..64),
                        palette = nextEntry<GuildCreate.Data.User.Collectibles.Nameplate.Palette>()
                    )
                }),
                bot = random.nextBoolean(),
                avatarDecorationData = random.takeRandomly {
                    GuildCreate.Data.AvatarDecorationData(
                        nextHash(),
                        nextSnowflake()
                    )
                },
                avatar = random.takeRandomly { nextHash() }
            ),
            roles = roles.takeRandomly(random, roleAmount) { it.id },
            premiumSince = random.takeRandomly { nextInstant() },
            pending = random.nextBoolean(),
            nick = random.takeRandomly { random.nextString(alphanumericChars, 3..32) },
            mute = random.nextBoolean(),
            joinedAt = random.nextInstant(),
            flags = Member.MemberFlag.toRaw(random.nextList(0..Member.MemberFlag.entries.size) { random.nextEntry<Member.MemberFlag>() }),
            deaf = random.nextBoolean(),
            communicationDisabledUntil = random.takeRandomly { nextInstant() },
            banner = random.takeRandomly { nextHash() },
            avatar = random.takeRandomly { nextHash() }
        )
    }

    context(random: Random)
    private fun createSoundboardSound(guildId: Snowflake, userIds: List<Snowflake>) = GuildCreate.Data.SoundboardSound(
        volume = random.nextDouble(0.0, 1.0),
        userId = userIds.random(random),
        soundId = random.nextSnowflake(),
        name = random.nextString(alphanumericChars, 3..32),
        guildId = random.takeRandomly { guildId },
        emojiName = random.takeRandomly { random.nextUnicodeEmoji() },
        emojiId = random.takeRandomly { random.nextSnowflake() },
        available = random.nextBoolean()
    )

    context(random: Random)
    private fun createVoiceState(channels: List<GuildCreate.Data.Channel>, userIds: List<Snowflake>) = GuildCreate.Data.VoiceState(
        channelId = channels.random(random).id,
        userId = userIds.random(random),
        sessionId = random.nextString(alphanumericChars, 3..32),
        deaf = random.nextBoolean(),
        mute = random.nextBoolean(),
        selfDeaf = random.nextBoolean(),
        selfMute = random.nextBoolean(),
        selfStream = random.nextBoolean(),
        selfVideo = random.nextBoolean(),
        suppress = random.nextBoolean(),
        requestToSpeakTimestamp = random.takeRandomly { nextInstant() }
    )

    context(random: Random)
    private fun createStageInstance(guildId: Snowflake, channels: List<GuildCreate.Data.Channel>, scheduledEvents: List<GuildCreate.Data.ScheduledEvent>): GuildCreate.Data.StageInstance {
        val maxTopicLength = random.fromDistribution(50 to 128, 50 to 256)

        return GuildCreate.Data.StageInstance(
            id = random.nextSnowflake(),
            guildId = guildId,
            channelId = channels.random(random).id,
            topic = random.nextString(words, 0..maxTopicLength),
            privacyLevel = 2,
            discoverableDisabled = random.nextBoolean(),
            guildScheduledEventId = random.takeRandomly { scheduledEvents.randomOrNull()?.id }
        )
    }

    context(random: Random)
    fun generate(sequence: Int): GuildCreate {
        val guildId = random.nextSnowflake()
        val hasPresenceIntent = false // Apparently you only get self member if you don't have presence intent
        val userIds = List(random.nextInt(1..1000)) { random.nextSnowflake() }
        val totalBoosts: Int
        val freeBoosts: Int
        val hasTags: Boolean
        val tagBadgeTier: Int
        val hasStyledRoles: Boolean
        // Max = 7 + 3 + 5 + 3 = 18
        totalBoosts = random.fromDistribution(65 to 0, 50 to 2, 20 to 5, 10 to 8, 5 to 11, 5 to 13, 2 to 18).also { totalBoosts ->
            var remainingBoosts = totalBoosts

            hasStyledRoles = remainingBoosts >= 3 && random.nextBoolean()
            if (hasStyledRoles) {
                remainingBoosts -= 3
            }

            hasTags = remainingBoosts >= 3 && random.nextBoolean()
            if (hasTags) {
                remainingBoosts -= 3
            }

            tagBadgeTier = when {
                remainingBoosts >= 5 -> random.nextInt(0..2)
                remainingBoosts >= 3 -> random.nextInt(0..1)
                else -> 0
            }
            if (tagBadgeTier == 1) {
                remainingBoosts -= 3
            } else if (tagBadgeTier == 2) {
                remainingBoosts -= 5
            }

            freeBoosts = remainingBoosts
        }
        val premiumTier = if (freeBoosts >= 7) {
            3
        } else if (freeBoosts >= 5) {
            2
        } else if (freeBoosts >= 2) {
            1
        } else {
            0
        }

        val roles = random.nextList(random.fromDistribution(50 to 0..<10, 30 to 10..<15, 15 to 15..<20, 4 to 20..<30, 2 to 30..50)) { createRole(premiumTier, hasStyledRoles) }
        val members = if (hasPresenceIntent) {
            val mutableUserIds = userIds.toMutableList()
            random.nextList(1..userIds.size) { createMember(mutableUserIds.removeAt(mutableUserIds.indices.random(random)), roles) }
        } else {
            List(1) { createMember(userIds.random(random), roles) /* self member */ }
        }
        val soundboardSounds = random.nextList(random.fromDistribution(45 to 0..<5, 8 to 5..<10, 4 to 10..<15, 2 to 15..20)) { createSoundboardSound(guildId, userIds) }
        val nonThreadChannels = random.nextList(random.fromDistribution(45 to 2..<10, 8 to 10..<20, 4 to 20..<30, 2 to 30..40)) { createChannel(isThread = false, roles, userIds) }
        val threadChannels = random.nextList(random.fromDistribution(50 to 0..0, 10 to 1..<4, 5 to 4..<6, 2 to 6..10)) { createChannel(isThread = true, roles, userIds) }
        val voiceStates = random.nextList(0..random.fromDistribution(50 to 0, 15 to 5, 8 to 8, 2 to 10).coerceAtMost(min(userIds.size, nonThreadChannels.size))) { createVoiceState(nonThreadChannels, userIds) }
        val scheduledEvents = random.nextList(random.fromDistribution(50 to 0..0, 10 to 1..<3, 2 to 3..4)) { createScheduledEvent(guildId, userIds, nonThreadChannels) }
        val stageInstances = random.nextList(0..nonThreadChannels.count { it.type.let(ChannelType::fromId) == STAGE }) { createStageInstance(guildId, nonThreadChannels, scheduledEvents) }
        val stickers = run {
            val maxStickers = when (premiumTier) {
                0 -> 5
                1 -> 15
                2 -> 30
                3 -> 60
                else -> throw AssertionError()
            }

            random.nextList(0..random.fromDistribution(40 to 2, 20 to 5, 10 to 8, 5 to 10).coerceAtMost(maxStickers)) { createSticker(guildId) }
        }
        val emojis = random.nextList(random.fromDistribution(45 to 0..<10, 20 to 10..<15, 8 to 20..<30, 4 to 30..<40, 2 to 40..50)) { createEmoji(roles) }

        return GuildCreate(
            sequence,
            GuildCreate.Data(
                features = features.takeRandomly(random, 2..6),
                premiumProgressBarEnabled = random.nextBoolean(),
                emojis = emojis,
                memberCount = userIds.size,
                rulesChannelId = random.takeRandomly { nextSnowflake() },
                voiceStates = voiceStates,
                large = random.nextBoolean(),
                vanityUrlCode = random.takeRandomly { random.nextString(letterChars, 3..20) },
                applicationId = null,
                icon = random.takeRandomly { nextHash() },
                stickers = stickers,
                splash = random.takeRandomly { nextHash() },
                systemChannelId = random.takeRandomly { nonThreadChannels.random(random).id },
                banner = random.takeRandomly { nextHash() },
                guildScheduledEvents = scheduledEvents,
                maxStageVideoChannelUsers = random.nextInt(0..4),
                id = guildId,
                verificationLevel = random.nextEntry<Guild.VerificationLevel>().key,
                maxMembers = 250000,
                stageInstances = stageInstances,
                safetyAlertsChannelId = random.takeRandomly { nonThreadChannels.random(random).id },
                name = random.nextString(alphanumericChars, 3..32),
                preferredLocale = random.nextEntry<DiscordLocale>().locale,
                maxVideoChannelUsers = random.nextInt(0..4),
                premiumSubscriptionCount = totalBoosts,
                threads = threadChannels,
                description = random.nextString(words, 0..random.fromDistribution(50 to 0, 10 to 256, 5 to 512, 2 to 1024)),
                mfaLevel = random.nextEntry<Guild.MFALevel>().key,
                systemChannelFlags = 1 shl random.nextInt(0..5),
                afkTimeout = random.nextInt(10..60),
                members = members,
                premiumTier = premiumTier,
                discoverySplash = random.takeRandomly { nextHash() },
                nsfwLevel = random.nextEntry<Guild.NSFWLevel>().key,
                explicitContentFilter = random.nextEntry<Guild.ExplicitContentLevel>().key,
                publicUpdatesChannelId = nonThreadChannels.random(random).id,
                afkChannelId = nonThreadChannels.random(random).id,
                unavailable = false,
                channels = nonThreadChannels,
                lazy = random.nextBoolean(),
                version = random.nextVersion(),
                ownerId = members.random(random).user.id,
                roles = roles,
                soundboardSounds = soundboardSounds,
                defaultMessageNotifications = random.nextInt(0..1),
                joinedAt = random.nextInstant(),
            )
        )
    }
}
