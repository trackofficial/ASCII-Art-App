package com.example.asciiartapp
 
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val btnOpenGallery = findViewById<Button>(R.id.button)
        btnOpenGallery.setOnClickListener {
            pickImage.launch("image/*")
        }
    }
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, CreateImageLayout::class.java).apply {
                putExtra("imageUri", it.toString())
            }
            startActivity(intent)
        }
    }
}