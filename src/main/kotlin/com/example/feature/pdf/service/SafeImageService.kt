package com.example.feature.pdf.service

import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

class SafeImageService {
    fun validDataUri(value: String?): String? {
        val input = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val match = DATA_URI.matchEntire(input)
        val declaredMime = match?.groupValues?.get(1)
        val encoded = match?.groupValues?.get(2) ?: input
        val bytes = runCatching { Base64.getDecoder().decode(encoded.filterNot(Char::isWhitespace)) }.getOrNull() ?: return null
        if (bytes.isEmpty() || bytes.size > MAX_IMAGE_BYTES) return null
        val mime = when {
            bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(PNG) -> "image/png"
            bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> "image/jpeg"
            else -> return null
        }
        if (declaredMime != null && declaredMime != mime) return null
        val image = runCatching { ImageIO.read(ByteArrayInputStream(bytes)) }.getOrNull() ?: return null
        if (image.width <= 0 || image.height <= 0 || image.width > MAX_DIMENSION || image.height > MAX_DIMENSION) return null
        return "data:$mime;base64,${Base64.getEncoder().encodeToString(bytes)}"
    }

    companion object {
        private const val MAX_IMAGE_BYTES = 2 * 1024 * 1024
        private const val MAX_DIMENSION = 4096
        private val DATA_URI = Regex("""data:(image/(?:png|jpeg));base64,(.+)""", RegexOption.IGNORE_CASE)
        private val PNG = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    }
}
