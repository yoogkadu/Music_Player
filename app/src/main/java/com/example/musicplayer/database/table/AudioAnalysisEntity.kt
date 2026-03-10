package com.example.musicplayer.database.table

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_analysis",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["hash"],
            childColumns = ["songHash"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songHash")]
)
data class AudioAnalysisEntity(
    @PrimaryKey val songHash: String,
    // --- FAISS & VGGish (On-Demand) ---
    val vggishEmbedding: FloatArray?,   // 128-dim Vector (Null until user triggers VGG)
    val faissIndexId: Int = -1,         // Position in the FAISS binary file
    val moodLabel: String? = null,      // e.g., "Energetic", "Chill"
    val genreLabel: String? = null,     // e.g., "C-Rock", "Lo-fi"

    // --- Essentia Background Extraction (Automatic) ---
    val mfccMean: FloatArray,           // 13 or 20 coefficients for timbre
    val chromaMean: FloatArray,         // 12-dim vector for harmony/key
    val bpm: Float,
    val danceability: Float,            // Essentia-specific metric
    val dissonance: Float,              // Measure of "harshness" in audio
    val spectralCentroid: Float,        // "Brightness" of the sound

    // --- Lifecycle Management ---
    val isEssentialAnalyzed: Boolean = false,
    val isVggAnalyzed: Boolean = false,
    val modelVersion: Int = 1,          // Triggers re-scan if you update PyTorch weights
    val analysisDate: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioAnalysisEntity

        if (faissIndexId != other.faissIndexId) return false
        if (bpm != other.bpm) return false
        if (danceability != other.danceability) return false
        if (dissonance != other.dissonance) return false
        if (spectralCentroid != other.spectralCentroid) return false
        if (isEssentialAnalyzed != other.isEssentialAnalyzed) return false
        if (isVggAnalyzed != other.isVggAnalyzed) return false
        if (modelVersion != other.modelVersion) return false
        if (analysisDate != other.analysisDate) return false
        if (songHash != other.songHash) return false
        if (!vggishEmbedding.contentEquals(other.vggishEmbedding)) return false
        if (moodLabel != other.moodLabel) return false
        if (genreLabel != other.genreLabel) return false
        if (!mfccMean.contentEquals(other.mfccMean)) return false
        if (!chromaMean.contentEquals(other.chromaMean)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faissIndexId
        result = 31 * result + bpm.hashCode()
        result = 31 * result + danceability.hashCode()
        result = 31 * result + dissonance.hashCode()
        result = 31 * result + spectralCentroid.hashCode()
        result = 31 * result + isEssentialAnalyzed.hashCode()
        result = 31 * result + isVggAnalyzed.hashCode()
        result = 31 * result + modelVersion
        result = 31 * result + analysisDate.hashCode()
        result = 31 * result + songHash.hashCode()
        result = 31 * result + (vggishEmbedding?.contentHashCode() ?: 0)
        result = 31 * result + (moodLabel?.hashCode() ?: 0)
        result = 31 * result + (genreLabel?.hashCode() ?: 0)
        result = 31 * result + mfccMean.contentHashCode()
        result = 31 * result + chromaMean.contentHashCode()
        return result
    }
}