package com.example.whatsappstatusmover

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ThumbnailUtil {
    companion object {
        private const val THUMBNAIL_SIZE = 200 // pixels
        private const val THUMBNAIL_QUALITY = 80 // 0-100
        private const val THUMBNAIL_DIR = "thumbnails"

        fun createThumbnail(sourceFile: File, baseDir: File): File? {
            try {
                // Create thumbnails directory if it doesn't exist
                val thumbnailDir = File(baseDir, THUMBNAIL_DIR)
                if (!thumbnailDir.exists()) {
                    thumbnailDir.mkdirs()
                }

                // Create thumbnail file with same name in thumbnails directory
                val thumbnailFile = File(thumbnailDir, sourceFile.name)

                // Skip if thumbnail already exists
                if (thumbnailFile.exists()) {
                    return thumbnailFile
                }

                // Calculate sample size for memory-efficient loading
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(sourceFile.absolutePath, options)

                val sampleSize = calculateSampleSize(
                    options.outWidth,
                    options.outHeight,
                    THUMBNAIL_SIZE
                )

                // Load bitmap with calculated sample size
                val bitmap = BitmapFactory.Options().run {
                    inSampleSize = sampleSize
                    inJustDecodeBounds = false
                    BitmapFactory.decodeFile(sourceFile.absolutePath, this)
                }

                // Scale to exact thumbnail size
                bitmap?.let { original ->
                    val width = original.width
                    val height = original.height
                    val scaleFactor = THUMBNAIL_SIZE.toFloat() / min(width, height)

                    val scaledBitmap = Bitmap.createScaledBitmap(
                        original,
                        (width * scaleFactor).toInt(),
                        (height * scaleFactor).toInt(),
                        true
                    )

                    // Save thumbnail
                    FileOutputStream(thumbnailFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, out)
                    }

                    // Clean up bitmaps
                    if (original != scaledBitmap) {
                        original.recycle()
                    }
                    scaledBitmap.recycle()

                    return thumbnailFile
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun calculateSampleSize(width: Int, height: Int, targetSize: Int): Int {
            var sampleSize = 1
            while ((width / sampleSize) > targetSize && (height / sampleSize) > targetSize) {
                sampleSize *= 2
            }
            return sampleSize
        }

        fun getThumbnailFile(originalFile: File, baseDir: File): File {
            return File(File(baseDir, THUMBNAIL_DIR), originalFile.name)
        }
    }
}