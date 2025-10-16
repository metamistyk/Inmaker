package com.example.inmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MenuActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity)

        auth = FirebaseAuth.getInstance()

        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)
        val pickFromGalleryButton = findViewById<Button>(R.id.pickFromGalleryButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Acción: tomar foto
        takePhotoButton.setOnClickListener {
            val intent = Intent(this, TakePhotoActivity::class.java)
            startActivity(intent)
        }

        // Acción: seleccionar imagen desde galería
        pickFromGalleryButton.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }

        // Acción: cerrar sesión y volver al login
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
