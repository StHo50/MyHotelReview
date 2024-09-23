package com.example.myhotelreview.service


import android.os.Handler
import android.os.Looper
import okhttp3.*
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody

class ImgurAPIservice {

    private val clientId = "5aafbbcbeead140"
    private val client = OkHttpClient()

    fun uploadImage(imageFile: File, callback: (Boolean, String?) -> Unit) {
        val mediaType = "image/*".toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, imageFile.asRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url("https://api.imgur.com/3/upload")
            .header("Authorization", "Client-ID $clientId")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    callback(false, null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (res.isSuccessful) {
                        val responseData = res.body?.string()
                        val imageUrl = extractImageUrl(responseData)
                        Handler(Looper.getMainLooper()).post {
                            callback(true, imageUrl)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            callback(false, null)
                        }
                    }
                }
            }
        })
    }

    private fun extractImageUrl(responseData: String?): String? {
        return responseData?.let {
            val regex = Regex("\"link\":\"(https://i.imgur.com/[^\"]+)\"")
            val matchResult = regex.find(it)
            matchResult?.groups?.get(1)?.value
        }
    }
}
