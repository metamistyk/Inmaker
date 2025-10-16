package com.example.inmaker.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface HuggingFaceService {

    @Multipart
    @POST("models/timbrooks/instruct-pix2pix")
    fun stylizeImage(
        @Part image: MultipartBody.Part,
        @Part("inputs") prompt: RequestBody
    ): Call<ResponseBody>

    companion object {
        private const val BASE_URL = "https://api-inference.huggingface.co/"

        fun create(token: String): HuggingFaceService {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(newRequest)
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(HuggingFaceService::class.java)
        }
    }
}
