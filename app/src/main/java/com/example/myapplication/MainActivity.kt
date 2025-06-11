package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    companion object {
        private const val PICK_FILE = 1
    }

    private lateinit var imageView: ImageView
    private lateinit var btnSend: Button
    private lateinit var resultSection: LinearLayout
    private lateinit var resultHeader: TextView
    private lateinit var nutrientText: TextView
    private lateinit var benefitText: TextView
    private lateinit var warningText: TextView
    private lateinit var requestQueue: RequestQueue
    private var imageUrl: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image_view)
        btnSend = findViewById(R.id.btnSend)
        resultSection = findViewById(R.id.result_section)
        resultHeader = findViewById(R.id.result_header)
        nutrientText = findViewById(R.id.nutrient_text)
        benefitText = findViewById(R.id.benefit_text)
        warningText = findViewById(R.id.warning_text)
        requestQueue = Volley.newRequestQueue(this)

        imageView.setOnClickListener { chooseImage() }
        btnSend.setOnClickListener { sendImageToServer() }

        val historyButton: LinearLayout = findViewById(R.id.history_button)
        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        val scanButton: LinearLayout = findViewById(R.id.scan_button)
        scanButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        val searchButton = findViewById<LinearLayout>(R.id.search_button)
        searchButton.setOnClickListener {
            val intent = Intent(this, SocKetActivity::class.java)
            startActivity(intent)
        }

    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageView.setImageURI(imageUri)
            imageUrl = imageUri.toString()
        }
    }

    private fun sendImageToServer() {
        if (imageUrl == null) {
            Toast.makeText(this, "Vui lòng chọn một hình ảnh trước", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap: Bitmap? = try {
            MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(imageUrl))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi tải hình ảnh", Toast.LENGTH_SHORT).show()
            return
        }

        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        val url = "http://192.168.1.139:5000/upload_and_predict" // cập nhật đúng endpoint

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    val predictedLabel = jsonResponse.getString("predicted_label")
                    val info = jsonResponse.getJSONObject("info")
                    val top3 = jsonResponse.getJSONArray("top3")
                    val imageUrl = jsonResponse.getString("image_url")

                    resultSection.visibility = View.VISIBLE
                    val top1 = top3.getJSONObject(0)
                    resultHeader.text = "${top1.getString("label")} - ${top1.getDouble("score")}%"

                    // Xử lý thanh_phan: Đổi thứ tự Tên trước, Giá trị sau, bỏ dấu :, xóa dấu , thừa
                    nutrientText.text = info.optString("thanh_phan", "Không có dữ liệu")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\\", "")
                        .split("{")
                        .map { item ->
                            val parts = item.trim().split(",")
                            val tenPart = parts.find { it.contains("ten") }?.replace("ten", "")?.replace(":", "")?.replace("\"", "")
                            val giaTriPart = parts.find { it.contains("gia_tri") }?.replace("gia_tri", "--- Giá trị ")?.replace(":", "")?.replace("\"", "")
                            if (tenPart != null && giaTriPart != null) "$tenPart, $giaTriPart".replace("},", "").replace("}", "")
                            else item.trim().replace("},", "").replace("}", "")
                        }
                        .joinToString("\n")
                    // Xử lý loi_ich: Đảm bảo xuống dòng sau mỗi thông tin trong "", xóa dấu , thừa
                    benefitText.text = info.optString("loi_ich", "Không có dữ liệu")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\\", "")
                        .split("\"")
                        .filter { it.isNotEmpty() }
                        .joinToString("\n") { it.trim().replace(",", "") }
                    warningText.text = info.optString("luu_y", "Không có dữ liệu")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\\", "")
                        .split("{")
                        .joinToString("\n") { it.trim().replace("}", "\n").replace("gia_tri", "Giá trị").replace("ten", "Tên").replace("\"", "") }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Lỗi JSON: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this@MainActivity, "Lỗi mạng: ${error.message}", Toast.LENGTH_LONG).show()
            }) {

            override fun getBodyContentType(): String {
                return "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"
            }

            override fun getBody(): ByteArray {
                val params = HashMap<String, String>()
                return getMultipartBody(params, byteArray)
            }
        }

        requestQueue.add(stringRequest)
    }




    private fun getMultipartBody(params: Map<String, String>, imageBytes: ByteArray): ByteArray {
        val boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
        val stringBuilder = StringBuilder()

        for ((key, value) in params) {
            stringBuilder.append("--").append(boundary).append("\r\n")
            stringBuilder.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n\r\n")
            stringBuilder.append(value).append("\r\n")
        }

        stringBuilder.append("--").append(boundary).append("\r\n")
        stringBuilder.append("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n")
        stringBuilder.append("Content-Type: image/jpeg\r\n\r\n")

        val bos = ByteArrayOutputStream()
        try {
            bos.write(stringBuilder.toString().toByteArray())
            bos.write(imageBytes)
            bos.write(("\r\n--$boundary--\r\n").toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bos.toByteArray()
    }
}