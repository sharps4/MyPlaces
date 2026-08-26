package com.example.myplaces.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

object BitmapUtils {
    fun createEmojiMarker(context: Context, emoji: String, size: Int = 100): Drawable {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            textSize = size * 0.8f
            textAlign = Paint.Align.CENTER
        }
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(emoji, xPos, yPos, paint)
        return BitmapDrawable(context.resources, bitmap)
    }
}
