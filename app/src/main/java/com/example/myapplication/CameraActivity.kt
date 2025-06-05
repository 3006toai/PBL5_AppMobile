package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.VolleyError

class CameraActivity : AppCompatActivity() {
    private val serverUrl = "http://192.168.1.92:5000" // Ensure this matches your Flask server IP
    private lateinit var webView: WebView
    private lateinit var imageView: ImageView
    private lateinit var btnCapture: Button
    private lateinit var resultSection: LinearLayout
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        webView = findViewById(R.id.webView)
        imageView = findViewById(R.id.camera_image_view)
        btnCapture = findViewById(R.id.btnCapture)
        resultSection = findViewById(R.id.result_section)
        resultText = findViewById(R.id.result_text)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("$serverUrl/video_feed")

        btnCapture.setOnClickListener {
            captureAndShowResult()
        }
    }

    private fun captureAndShowResult() {
        val queue = Volley.newRequestQueue(this)
        val url = "$serverUrl/capture_and_predict"
        val request = object : JsonObjectRequest(Method.POST, url, null,
            { response ->
                try {
                    // Extract image and prediction data
                    val imageBase64 = response.optString("image_base64")
                    val predictedLabel = response.optString("predicted_label")
                    val top3 = response.optJSONArray("top3")

                    // Show captured image
                    if (imageBase64.isNotEmpty()) {
                        val imageBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imageView.setImageBitmap(bitmap)
                        imageView.visibility = View.VISIBLE
                        webView.visibility = View.GONE
                    } else {
                        Toast.makeText(this, "Không nhận được ảnh từ server", Toast.LENGTH_SHORT).show()
                    }

                    // Show prediction result below image
                    val top1 = top3?.optJSONObject(0)
                    val label = top1?.optString("label") ?: predictedLabel
                    val score = top1?.optDouble("score") ?: 0.0
                    if (label.isNotEmpty()) {
                        resultText.text = "Kết quả: $label (${String.format("%.2f", score)}%)"
                        resultSection.visibility = View.VISIBLE
                    } else {
                        resultText.text = "Kết quả: Không xác định"
                        resultSection.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Lỗi xử lý dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error: VolleyError ->
                val errorMsg = error.message ?: "Không thể kết nối đến server"
                Toast.makeText(this, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
            }) {}
        queue.add(request)
    }
}