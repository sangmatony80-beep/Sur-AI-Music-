package com.ai.audio.infrastructure.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class CryptographicWatermarker {

    // অডিও ট্র্যাকে এমবেড করার জন্য ক্রিপ্টোগ্রাফিক সিক্রেট টোকেন জেনারেট করা
    private fun generateWatermarkToken(licenseId: String, secretKey: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secretKey, "HmacSHA256")
        mac.init(secretKeySpec)
        return mac.doFinal(licenseId.toByteArray(Charsets.UTF_8))
    }

    /**
     * অডিওর LSB (Least Significant Bit)-এ অদৃশ্য ওয়াটারমার্ক এমবেড করা
     * এটি অডিও কোয়ালিটি (320kbps) নষ্ট না করে ডেটা প্রোটেকশন নিশ্চিত করে।
     */
    fun embedWatermark(originalPcmData: ByteArray, licenseId: String, secretKey: ByteArray): ByteArray {
        val watermarkBytes = generateWatermarkToken(licenseId, secretKey)
        val watermarkedData = originalPcmData.copyOf()

        var watermarkBitIndex = 0
        val totalWatermarkBits = watermarkBytes.size * 8

        // অডিও সিগন্যালের সাব-ফ্রিকোয়েন্সিতে বিট ম্যানিপুলেশন (LSB Steganography)
        for (i in watermarkedData.indices) {
            if (watermarkBitIndex >= totalWatermarkBits) break

            // প্রতি ৪টি স্যাম্পল পর পর ১ বিট করে ডাটা হাইড করা (অডিও ফিডেলিটি অক্ষুণ্ণ রাখতে)
            if (i % 4 == 0) {
                val byteIndex = watermarkBitIndex / 8
                val bitIndex = watermarkBitIndex % 8
                val currentBit = (watermarkBytes[byteIndex].toInt() ushr (7 - bitIndex)) and 1

                // LSB ক্লিয়ার করে ওয়াটারমার্ক বিট বসানো
                watermarkedData[i] = ((watermarkedData[i].toInt() and 0xFE) or currentBit).toByte()
                watermarkBitIndex++
            }
        }
        return watermarkedData
    }
}
