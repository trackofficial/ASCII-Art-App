package com.example.asciiartapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

//Пометки делал для себя
class CreateImageLayout : ComponentActivity() {
    private lateinit var original: Bitmap
    private var currentBitmap: Bitmap? = null
    // Наборы символов
    private val charsetLow    = " .:-=+*#@"      // 9
    private val charsetMedium = " .,:;irsXA"     // 10
    private val charsetHigh   = " .:-=+*#%@"     // 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_screen)
        val seekbar = findViewById<SeekBar>(R.id.seekBar)
        val asciiTextView = findViewById<TextView>(R.id.text_asciiart)
        val copybutton = findViewById<Button>(R.id.buttoncopy)
        copybutton.setOnClickListener {
            val textToCopy = asciiTextView.text.toString()
            copyToClipboard(textToCopy)
        }
        // 1. Достаём Uri из интента
        val uriString = intent.getStringExtra("imageUri")
        if (uriString.isNullOrEmpty()) {
            Toast.makeText(this, "Нет изображения для загрузки", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val uri = Uri.parse(uriString)
        // 2. Загружаем bitmap из Uri
        val sourceBitmap = loadBitmapFromUri(uri)
        if (sourceBitmap == null) {
            Toast.makeText(this, "Не удалось загрузить изображение", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        // 3. Переводим в RGB и Создаём "оригинал" нужного размера для показа и дальнейшей работы
        val mutableBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, false)
        val targetHeight = 800
        val aspectRatio = mutableBitmap.width.toFloat() / mutableBitmap.height.toFloat()
        val targetWidth = (targetHeight * aspectRatio).toInt()
        original = Bitmap.createScaledBitmap(mutableBitmap, targetWidth, targetHeight, true)
        currentBitmap = original
        // 4. Стартовое значение seekBar и первый ASCII
        if (seekbar.progress == 0) seekbar.progress = 50
        val startWidth = seekbar.progress

        var asciiBitmap = makeLowQualityCopy(original, targetWidth = startWidth)
        asciiTextView.text = bitmapToAscii(
            asciiBitmap,
            width = startWidth,
            chars = pickCharset(startWidth)
        )
        // 5. Обновление при движении ползунка
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val w = progress.coerceAtLeast(10) // минимум 10
                asciiBitmap = makeLowQualityCopy(original, targetWidth = w)
                asciiTextView.text = bitmapToAscii(
                    asciiBitmap,
                    width = w,
                    chars = pickCharset(w)
                )
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun pickCharset(width: Int): String {
        return when {
            width < 40  -> charsetLow
            width < 80  -> charsetMedium
            else        -> charsetHigh
        }
    }

    fun makeLowQualityCopy(
        bitmap: Bitmap,
        targetWidth: Int = 100,
        config: Bitmap.Config = Bitmap.Config.RGB_565
    ): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetHeight = (targetWidth / aspectRatio).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        return scaled.copy(config, false)
    }

    //Основаня функция конвертирования
    fun bitmapToAscii(
        bitmap: Bitmap,
        width: Int = 80,
        chars: String = "@%#*+=-:. "
    ): String {
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            width,
            (bitmap.height * width / bitmap.width.toFloat() / 2).toInt(),
            true
        )
        val sb = StringBuilder()
        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF)
                val g = (pixel shr 8 and 0xFF)
                val b = (pixel and 0xFF)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                val idx = ((gray / 255.0) * (chars.lastIndex)).toInt()
                sb.append(chars[idx])
            }
            sb.append('\n')
        }
        return sb.toString()
    }

    //Функция для копирования текста
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Текст скопирован", Toast.LENGTH_SHORT).show()
    }
}

