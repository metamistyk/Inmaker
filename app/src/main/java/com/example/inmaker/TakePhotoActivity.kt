package com.example.inmaker

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.inmaker.network.HuggingFaceService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class TakePhotoActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var stylizeButton: Button
    private lateinit var progressBar: ProgressBar
    private var imageUri: Uri? = null

    // 🔑 Inserta aquí tu token de Hugging Face
    private val hfToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.take_photo_activity)

        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        stylizeButton = findViewById(R.id.stylizeButton)
        progressBar = findViewById(R.id.progressBar)

        takePhotoButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
        }

        stylizeButton.setOnClickListener {
            imageUri?.let {
                processImageWithAI(it)
            } ?: Toast.makeText(this, "Primero toma una foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? android.graphics.Bitmap
            photo?.let {
                val file = File(cacheDir, "photo.jpg")
                val out = FileOutputStream(file)
                it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                imageUri = Uri.fromFile(file)
                imageView.setImageBitmap(it)
            }
        }
    }

    private fun processImageWithAI(imageUri: Uri) {
        progressBar.visibility = ProgressBar.VISIBLE

        val file = File(imageUri.path!!)
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        val prompt = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            "Make this product photo look professional, clean white background, well lit, high quality for e-commerce"
        )

        val service = HuggingFaceService.create(hfToken)
        val call = service.stylizeImage(body, prompt)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressBar.visibility = ProgressBar.GONE
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val bytes = body.bytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.setImageBitmap(bitmap)
                        Toast.makeText(applicationContext, "Imagen estilizada con IA 😎", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Error en la respuesta de IA", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
