package com.example.myplaces.util

import android.content.Context
import java.io.File

object PhotoStorage {

    fun photosDir(context: Context): File =
        File(context.filesDir, "photos").apply { mkdirs() }

    fun newPhotoFile(context: Context): File =
        File(photosDir(context), "photo_${System.currentTimeMillis()}.jpg")
}