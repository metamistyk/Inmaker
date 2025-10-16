package com.example.inmaker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class GalleryActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var pickButton: Button
    private lateinit var uploadButton: Button

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 100

    private val storageRef = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)

        imageView = findViewById(R.id.imageView)
        pickButton = findViewById(R.id.pickButton)
        uploadButton = findViewById(R.id.uploadButton)

        // Seleccionar imagen desde galería
        pickButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Subir imagen a Firebase
        uploadButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                uploadImageToFirebase(uri)
            } ?: Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val fileName = "imagen_${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child("imagenes/$fileName")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Guardar datos en Firestore
                    val data = hashMapOf(
                        "nombre" to "Ejemplo de imagen",
                        "descripcion" to "Imagen subida desde la galería",
                        "url" to downloadUrl.toString(),
                        "fecha_subida" to Date()
                    )
                    db.collection("imagenes_estilizadas").add(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Imagen subida y guardada correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir imagen: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
