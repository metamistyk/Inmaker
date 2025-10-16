package com.example.inmaker

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class GalleryActivity : AppCompatActivity() {
    // cambios
    private lateinit var imageView: ImageView
    private lateinit var pickButton: Button
    private lateinit var uploadButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)

        imageView = findViewById(R.id.imageView)
        pickButton = findViewById(R.id.pickButton)
        uploadButton = findViewById(R.id.uploadButton)
        nameEditText = findViewById(R.id.productNameEditText)
        priceEditText = findViewById(R.id.productPriceEditText)
        descriptionEditText = findViewById(R.id.productDescriptionEditText)

        pickButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        uploadButton.setOnClickListener {
            uploadToFirebase()
        }
    }

    private fun uploadToFirebase() {
        val name = nameEditText.text.toString().trim()
        val price = priceEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (imageUri == null || name.isEmpty() || price.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo datos...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val fileName = "images/${UUID.randomUUID()}.jpg"
        val imageRef = storage.child(fileName)

        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val product = hashMapOf(
                        "name" to name,
                        "price" to price.toDoubleOrNull(),
                        "description" to description,
                        "imageUrl" to uri.toString(),
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("products")
                        .add(product)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Producto subido correctamente", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MenuActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(this, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
