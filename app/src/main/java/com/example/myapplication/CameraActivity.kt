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
    private val serverUrl = "http://172.20.10.6:5000" // Ensure this matches your Flask server IP
    private lateinit var webView: WebView
    private lateinit var imageView: ImageView
    private lateinit var btnCapture: Button
    private lateinit var resultSection: LinearLayout
    private lateinit var resultText: TextView
    private lateinit var nutrientText: TextView
    private lateinit var benefitText: TextView
    private lateinit var warningText: TextView
    private lateinit var resultHeader: TextView
    private lateinit var queue: com.android.volley.RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        queue = Volley.newRequestQueue(this)

        webView = findViewById(R.id.webView)
        imageView = findViewById(R.id.camera_image_view)
        btnCapture = findViewById(R.id.btnCapture)
        resultSection = findViewById(R.id.result_section)
        resultText = findViewById(R.id.result_text)
        nutrientText = findViewById(R.id.nutrient_text)
        benefitText = findViewById(R.id.benefit_text)
        warningText = findViewById(R.id.warning_text)

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
                    // Lấy dữ liệu từ response
                    val imageBase64 = response.optString("image_base64")
                    val predictedLabel = response.optString("predicted_label")
                    val top3 = response.optJSONArray("top3")
                    val info = response.optJSONObject("info")

                    // Hiển thị ảnh đã chụp
                    if (imageBase64.isNotEmpty()) {
                        val imageBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imageView.setImageBitmap(bitmap)
                        imageView.visibility = View.VISIBLE
                        webView.visibility = View.GONE
                    } else {
                        Toast.makeText(this, "Không nhận được ảnh từ server", Toast.LENGTH_SHORT).show()
                    }

                    // Hiển thị kết quả dự đoán
                    val top1 = top3?.optJSONObject(0)
                    val label = top1?.optString("label") ?: predictedLabel
                    val score = top1?.optDouble("score") ?: 0.0

                    if (label.isNotEmpty()) {
                        resultText.text = "🌿 Kết quả: $label (${String.format("%.2f", score)}%)"
                    } else {
                        resultText.text = "Kết quả: Không xác định"
                    }

                    // Hiển thị thông tin cây
                    val plantInfo = response.optJSONObject("info")

                    // Xử lý thanh_phan: Đổi thứ tự Tên trước, Giá trị sau, định dạng Tên..., --- Giá trị..., xóa dấu , thừa
                    nutrientText.text = plantInfo?.optString("thanh_phan", "Không có dữ liệu")
                        ?.replace("[", "")
                        ?.replace("]", "")
                        ?.replace("\\", "")
                        ?.split("{")
                        ?.map { item ->
                            val parts = item.trim().split(",")
                            val tenPart = parts.find { it.contains("ten") }?.replace("ten", "")?.replace(":", "")?.replace("\"", "")
                            val giaTriPart = parts.find { it.contains("gia_tri") }?.replace("gia_tri", "--- Giá trị ")?.replace(":", "")?.replace("\"", "")
                            if (tenPart != null && giaTriPart != null) "$tenPart, $giaTriPart".replace("},", "").replace("}", "")
                            else item.trim().replace("},", "").replace("}", "")
                        }
                        ?.joinToString("\n") ?: "Không có dữ liệu"

                    // Xử lý loi_ich: Đảm bảo xuống dòng sau mỗi thông tin trong "", xóa dấu , thừa
                    benefitText.text = plantInfo?.optString("loi_ich", "Không rõ")
                        ?.replace("[", "")
                        ?.replace("]", "")
                        ?.replace("\\", "")
                        ?.split("\"")
                        ?.filter { it.isNotEmpty() }
                        ?.joinToString("\n") { it.trim().replace(",", "") } ?: "Không rõ"

                    // Xử lý luu_y: Đảm bảo xuống dòng sau mỗi thông tin
                    warningText.text = plantInfo?.optString("luu_y", "Không rõ")
                        ?.replace("[", "")
                        ?.replace("]", "")
                        ?.replace("\\", "")
                        ?.split("{")
                        ?.joinToString("\n") { it.trim().replace("}", "\n").replace("gia_tri", "Giá trị").replace("ten", "Tên").replace("\"", "") } ?: "Không rõ"

                    resultSection.visibility = View.VISIBLE
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