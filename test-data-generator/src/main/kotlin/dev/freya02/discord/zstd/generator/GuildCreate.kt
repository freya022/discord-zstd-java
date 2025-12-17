package dev.freya02.discord.zstd.generator

import java.time.Instant

typealias Snowflake = String
typealias Hash = String
typealias Permissions = String

@Suppress("unused")
class GuildCreate(
    val s: Int,
    val d: Data,
) {

    val t = "GUILD_CREATE"
    val op = 0

    class Data(
        val features: Set<String>,
        val premiumProgressBarEnabled: Boolean,
        val emojis: List<Emoji>,
        val memberCount: Int,
        val rulesChannelId: Snowflake?,
        val voiceStates: List<VoiceState>,
        val large: Boolean,
        val vanityUrlCode: String?,
        val applicationId: Snowflake?,
        val icon: Hash?,
        val stickers: List<Sticker>,
        val splash: Hash?,
        val systemChannelId: Snowflake?,
        val banner: Hash?,
        val guildScheduledEvents: List<ScheduledEvent>,
        val maxStageVideoChannelUsers: Int,
        val id: Snowflake,
        val verificationLevel: Int,
        val maxMembers: Int,
        val stageInstances: List<StageInstance>,
        val safetyAlertsChannelId: Snowflake?,
        val name: String,
        val preferredLocale: String,
        val maxVideoChannelUsers: Int,
        val premiumSubscriptionCount: Int,
        val threads: List<Channel>,
        val description: String,
        val mfaLevel: Int,
        val systemChannelFlags: Int,
        val afkTimeout: Int,
        val members: List<Member>,
        val premiumTier: Int,
        val discoverySplash: Hash?,
        val nsfwLevel: Int,
        val explicitContentFilter: Int,
        val publicUpdatesChannelId: Snowflake?,
        val afkChannelId: Snowflake?,
        val unavailable: Boolean,
        val channels: List<Channel>,
        val lazy: Boolean,
        val nsfw: Boolean = false,
        val version: Long,
        val ownerId: Snowflake,
        val roles: List<Role>,
        val soundboardSounds: List<SoundboardSound>,
        val defaultMessageNotifications: Int,
        val joinedAt: Instant,
    ) {

        val ownerConfiguredContentLevel: Int = 0
        val inventorySettings: Any? = null
        val homeHeader: Any? = null
        val incidentsData: Any? = null
        val profile: Any? = null
        val region: String = "deprecated"
        val embeddedActivities: List<Nothing> = emptyList()
        val activityInstances: List<Nothing> = emptyList()
        val latestOnboardingQuestionId: Snowflake? = null
        val applicationCommandCounts: Map<Snowflake, Int> = emptyMap()
        val hubType: Any? = null
        val moderatorReporting: Any? = null
        val premiumFeatures: Any? = null
        val presences: List<Nothing> = emptyList()

        class Emoji(
            val version: Long,
            val roles: Set<Snowflake>,
            val requiresColons: Boolean,
            val name: String,
            val managed: Boolean,
            val id: Snowflake,
            val available: Boolean,
            val animated: Boolean,
        )

        class VoiceState(
            val channelId: Snowflake,
            val userId: Snowflake,
            val sessionId: String,
            val deaf: Boolean,
            val mute: Boolean,
            val selfDeaf: Boolean,
            val selfMute: Boolean,
            val selfStream: Boolean,
            val selfVideo: Boolean,
            val suppress: Boolean,
            val requestToSpeakTimestamp: Instant?,
        )

        class Member(
            val user: User,
            val roles: Set<Snowflake>,
            val premiumSince: Instant?,
            val pending: Boolean,
            val nick: String?,
            val mute: Boolean,
            val joinedAt: Instant,
            val flags: Int,
            val deaf: Boolean,
            val communicationDisabledUntil: Instant?,
            val banner: Hash?,
            val avatar: Hash?,
        )

        class User(
            val username: String,
            val publicFlags: Int,
            val primaryGuild: PrimaryGuild?,
            val id: Snowflake,
            val globalName: String?,
            val displayName: String?,
            val discriminator: String,
            val collectibles: Collectibles,
            val bot: Boolean,
            val avatarDecorationData: AvatarDecorationData?,
            val avatar: Hash?,
        ) {
            val displayNameStyles: Any? = null

            class PrimaryGuild(
                val identityGuildId: Snowflake,
                val identityEnabled: Boolean,
                val tag: String,
                val badge: Hash,
            )

            class Collectibles(
                val nameplate: Nameplate?
            ) {
                class Nameplate(
                    val skuId: Snowflake,
                    val asset: String,
                    val label: String,
                    val palette: Palette,
                ) {
                    @Suppress("EnumEntryName")
                    enum class Palette {
                        crimson,
                        berry,
                        sky,
                        teal,
                        forest,
                        bubble_gum,
                        violet,
                        cobalt,
                        clover,
                        lemon,
                        white,
                    }
                }
            }
        }

        class AvatarDecorationData(
            val asset: Hash,
            val skuId: Snowflake,
        )

        class Sticker(
            val version: Long,
            val type: Int,
            val tags: String,
            val name: String,
            val id: Snowflake,
            val guildId: Snowflake?,
            val formatType: Int,
            val description: String,
            val available: Boolean,
        ) {
            val asset: String = ""
        }

        class ScheduledEvent(
            val id: Snowflake,
            val guildId: Snowflake,
            val channelId: Snowflake?,
            val creatorId: Snowflake,
            val name: String,
            val description: String?,
            val scheduledStartTime: Instant,
            val scheduledEndTime: Instant?,
        )

        class StageInstance(
            val id: Snowflake,
            val guildId: Snowflake,
            val channelId: Snowflake,
            val topic: String,
            val privacyLevel: Int,
            val discoverableDisabled: Boolean,
            val guildScheduledEventId: Snowflake?,
        )

        open class Channel(
            val id: Snowflake,
            val type: Int,
            val version: Long,
            val position: Int?,
            val permissionOverwrites: List<Overwrite>,
            val name: String,
            val topic: String?,
            val nsfw: Boolean,
            val lastMessageId: Snowflake,
            val lastPinTimestamp: Instant?,
            val parentId: Snowflake?,
            val flags: Int,
            val defaultAutoArchiveDuration: Int,
        ) {
            class Overwrite(
                val id: Snowflake,
                val type: Int,
                val allow: Permissions,
                val deny: Permissions,
            )
        }

        class Role(
            val version: Long,
            val unicodeEmoji: String?,
            val tags: Tags,
            val position: Int,
            val permissions: Permissions,
            val name: String,
            val mentionable: Boolean,
            val managed: Boolean,
            val id: Snowflake,
            val icon: Hash?,
            val hoist: Boolean,
            val flags: Int,
            val colors: Colors,
        ) {
            val color: Int = 0

            class Tags(
                val botId: Snowflake?,
            )

            class Colors(
                val primaryColor: Int,
                val secondaryColor: Int,
                val tertiaryColor: Int,
            )
        }

        class SoundboardSound(
            val volume: Double,
            val userId: Snowflake,
            val soundId: Snowflake,
            val name: String,
            val guildId: Snowflake?,
            val emojiName: String?,
            val emojiId: Snowflake?,
            val available: Boolean,
        )
    }
}
