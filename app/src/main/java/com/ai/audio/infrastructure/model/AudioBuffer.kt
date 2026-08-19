package com.ai.audio.infrastructure.model

data class AudioBuffer(
    val data: FloatArray,
    val sampleRate: Int = 48000
) {
    fun toPcm16BitByteArray(): ByteArray {
        val byteArray = ByteArray(data.size * 2)
        for (i in data.indices) {
            val shortVal = (data[i].coerceIn(-1.0f, 1.0f) * 32767.0f).toInt().toShort()
            byteArray[i * 2] = (shortVal.toInt() and 0xFF).toByte()
            byteArray[i * 2 + 1] = ((shortVal.toInt() ushr 8) and 0xFF).toByte()
        }
        return byteArray
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioBuffer

        if (!data.contentEquals(other.data)) return false
        if (sampleRate != other.sampleRate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sampleRate
        return result
    }
}
