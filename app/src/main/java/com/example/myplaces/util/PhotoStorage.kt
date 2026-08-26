package com.example.myplaces.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object PhotoStorage {

    private const val PHOTOS_DIR = "photos"
    private const val EXPORTS_DIR = "exports"

    fun photosDir(context: Context): File =
        File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }

    fun exportsDir(context: Context): File =
        File(context.cacheDir, EXPORTS_DIR).apply { mkdirs() }
    fun newPhotoFile(context: Context): File =
        File(photosDir(context), "photo_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")

    fun shareableUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    suspend fun importFromUri(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        val target = newPhotoFile(context)
        runCatching {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            target.absolutePath
        }.getOrElse {
            target.delete()
            null
        }
    }
    suspend fun writeBytes(context: Context, bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        val target = newPhotoFile(context)
        runCatching {
            target.writeBytes(bytes)
            target.absolutePath
        }.getOrElse {
            target.delete()
            null
        }
    }

    suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        if (path.isNullOrBlank()) return@withContext
        runCatching { File(path).delete() }
    }
    suspend fun encodeToBase64(path: String?): String? = withContext(Dispatchers.IO) {
        if (path.isNullOrBlank()) return@withContext null
        val file = File(path)
        if (!file.exists()) return@withContext null
        runCatching { Base64.encodeToString(file.readBytes(), Base64.NO_WRAP) }.getOrNull()
    }
    suspend fun decodeFromBase64(context: Context, base64: String?): String? {
        if (base64.isNullOrBlank()) return null
        val bytes = runCatching { Base64.decode(base64, Base64.NO_WRAP) }.getOrNull() ?: return null
        return writeBytes(context, bytes)
    }
}
