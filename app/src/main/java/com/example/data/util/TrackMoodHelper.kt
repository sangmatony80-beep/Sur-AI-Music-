package com.example.data.util

import com.example.data.local.SongEntity
import com.example.data.supabase.RemoteSongItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class MoodTagInfo(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val emoji: String,
    val gradientColors: List<Long>, // ARGB hex
    val keywords: List<String>
)

object TrackMoodHelper {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val PRESET_MOODS = listOf(
        MoodTagInfo(
            id = "all",
            nameEn = "All Moods",
            nameBn = "সব মুড",
            emoji = "✨",
            gradientColors = listOf(0xFF6366F1, 0xFF8B5CF6),
            keywords = emptyList()
        ),
        MoodTagInfo(
            id = "romantic",
            nameEn = "Romantic",
            nameBn = "রোমান্টিক",
            emoji = "🌸",
            gradientColors = listOf(0xFFEC4899, 0xFFF43F5E),
            keywords = listOf("romantic", "love", "প্রেম", "ভালোবাসা", "মায়া", "হৃদয়", "প্রিয়", "sweet", "ballad")
        ),
        MoodTagInfo(
            id = "energetic",
            nameEn = "Energetic",
            nameBn = "উচ্ছ্বসিত ও চনমনে",
            emoji = "🔥",
            gradientColors = listOf(0xFFF97316, 0xFFEF4444),
            keywords = listOf("energetic", "hype", "edm", "rock", "fast", "dance", "beat", "নাচ", "দম", "উদ্দাম", "power")
        ),
        MoodTagInfo(
            id = "melancholic",
            nameEn = "Melancholic",
            nameBn = "বিরহী ও স্মৃতিকাতর",
            emoji = "🌙",
            gradientColors = listOf(0xFF3B82F6, 0xFF6366F1),
            keywords = listOf("sad", "melancholic", "lonely", "বিরহ", "কষ্ট", "স্মৃতি", "অশ্রু", "বেদনা", "blue", "heartbreak")
        ),
        MoodTagInfo(
            id = "chill",
            nameEn = "Chill & Lofi",
            nameBn = "শান্ত ও নির্জন",
            emoji = "🧘",
            gradientColors = listOf(0xFF10B981, 0xFF14B8A6),
            keywords = listOf("chill", "lofi", "relax", "peace", "শান্ত", "ঘুম", "নিস্তব্ধ", "meditation", "calm", "soft")
        ),
        MoodTagInfo(
            id = "rainy",
            nameEn = "Rainy Nostalgia",
            nameBn = "বৃষ্টির নস্টালজিয়া",
            emoji = "🌧️",
            gradientColors = listOf(0xFF0EA5E9, 0xFF2563EB),
            keywords = listOf("rain", "rainy", "বৃষ্টি", "বাদল", "বর্ষা", "মেঘ", "শ্রাবণ", "nostalgia", "মন খারাপ")
        ),
        MoodTagInfo(
            id = "party",
            nameEn = "Party & Club",
            nameBn = "পার্টি ও উৎসব",
            emoji = "🎉",
            gradientColors = listOf(0xFFA855F7, 0xFFEC4899),
            keywords = listOf("party", "club", "dj", "উৎসবে", "আনন্দ", "ধামাকা", "festival", "celebration", "banger")
        ),
        MoodTagInfo(
            id = "cyber_hype",
            nameEn = "Cyber Synth",
            nameBn = "সাইবার হাইপ",
            emoji = "⚡",
            gradientColors = listOf(0xFF8B5CF6, 0xFF06B6D4),
            keywords = listOf("cyberpunk", "synthwave", "future", "ai", "ইলেক্ট্রো", "synth", "futuristic", "neon")
        ),
        MoodTagInfo(
            id = "folk",
            nameEn = "Acoustic Folk",
            nameBn = "বাউল ও মাটির সুর",
            emoji = "🎸",
            gradientColors = listOf(0xFFD97706, 0xFFB45309),
            keywords = listOf("folk", "acoustic", "বাউল", "পল্লী", "ভাটিয়ালী", "একতারা", "unplugged", "village", "roots")
        ),
        MoodTagInfo(
            id = "spiritual",
            nameEn = "Spiritual",
            nameBn = "আধ্যাত্মিক ও মরমী",
            emoji = "🕊️",
            gradientColors = listOf(0xFF059669, 0xFF10B981),
            keywords = listOf("spiritual", "sufi", "মরমী", "সুফি", "ভক্তি", "খোদা", "প্রার্থনা", "devotional", "soul")
        )
    )

    /**
     * Extracts all mood tags for a song entity by checking Supabase metadata JSON,
     * title, genre, and lyrics prompt keywords.
     */
    fun extractMoodTags(song: SongEntity): List<MoodTagInfo> {
        val detected = mutableSetOf<MoodTagInfo>()
        val combinedText = "${song.title} ${song.artist} ${song.genre} ${song.lyrics}".lowercase()

        // 1. Check if genre or title directly matches a mood
        for (mood in PRESET_MOODS.drop(1)) {
            if (mood.keywords.any { combinedText.contains(it) }) {
                detected.add(mood)
            }
        }

        // 2. Deterministic mood tag fallback based on hash so every track has 1-3 rich tags
        if (detected.isEmpty()) {
            val hash = Math.abs((song.title + song.genre).hashCode())
            val moodIndex1 = (hash % (PRESET_MOODS.size - 1)) + 1
            val moodIndex2 = ((hash / 7) % (PRESET_MOODS.size - 1)) + 1
            detected.add(PRESET_MOODS[moodIndex1])
            if (moodIndex1 != moodIndex2) {
                detected.add(PRESET_MOODS[moodIndex2])
            }
        }

        return detected.toList()
    }

    /**
     * Extracts mood tags from a remote Supabase item metadata
     */
    fun extractRemoteMoodTags(item: RemoteSongItem): List<MoodTagInfo> {
        val detected = mutableSetOf<MoodTagInfo>()

        // Check metadata JSON from Supabase
        if (!item.metadata.isNullOrBlank()) {
            try {
                val obj = json.parseToJsonElement(item.metadata).jsonObject
                obj["mood"]?.jsonPrimitive?.content?.let { m ->
                    findMoodByName(m)?.let { detected.add(it) }
                }
                obj["mood_tags"]?.jsonArray?.forEach { elem ->
                    findMoodByName(elem.jsonPrimitive.content)?.let { detected.add(it) }
                }
                obj["vibe"]?.jsonPrimitive?.content?.let { v ->
                    findMoodByName(v)?.let { detected.add(it) }
                }
                obj["tags"]?.jsonArray?.forEach { elem ->
                    findMoodByName(elem.jsonPrimitive.content)?.let { detected.add(it) }
                }
            } catch (_: Exception) {}
        }

        // Fallback to keyword matching from title, artist, genre, prompt
        if (detected.isEmpty()) {
            val combined = "${item.title} ${item.artist} ${item.genre} ${item.prompt}".lowercase()
            for (mood in PRESET_MOODS.drop(1)) {
                if (mood.keywords.any { combined.contains(it) }) {
                    detected.add(mood)
                }
            }
        }

        if (detected.isEmpty()) {
            val hash = Math.abs((item.title + item.genre).hashCode())
            val idx = (hash % (PRESET_MOODS.size - 1)) + 1
            detected.add(PRESET_MOODS[idx])
        }

        return detected.toList()
    }

    private fun findMoodByName(name: String): MoodTagInfo? {
        val clean = name.trim().lowercase()
        return PRESET_MOODS.drop(1).find {
            it.id == clean ||
            it.nameEn.equals(clean, ignoreCase = true) ||
            it.nameBn.equals(clean, ignoreCase = true) ||
            it.keywords.any { kw -> clean.contains(kw) }
        }
    }
}
