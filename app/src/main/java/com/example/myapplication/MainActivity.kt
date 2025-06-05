package com.example.myapplication

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
import org.json.JSONArray
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

        val url = "http://10.0.2.2:5000/api/predict"
        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String> { response ->
                // Debug: Hiển thị phản hồi thô từ server
                Toast.makeText(this@MainActivity, "Phản hồi: $response", Toast.LENGTH_LONG).show()
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val predictions: JSONArray = jsonResponse.getJSONArray("predictions")
                        val plantInfo: JSONObject = jsonResponse.getJSONObject("plant_info")
                        val imageUrl: String = jsonResponse.getString("image_url")

                        resultSection.visibility = View.VISIBLE
                        resultHeader.text = "${predictions.getJSONObject(0).getString("label")} - ${predictions.getJSONObject(0).getDouble("score")}%"
                        nutrientText.text = "Thành phần: ${if (plantInfo.has("thanh_phan")) plantInfo.getString("thanh_phan") else "Không có dữ liệu"}"
                        benefitText.text = "Lợi ích: ${if (plantInfo.has("loi_ich")) plantInfo.getString("loi_ich") else "Không có dữ liệu"}"
                        warningText.text = "Lưu ý: ${if (plantInfo.has("luu_y")) plantInfo.getString("luu_y") else "Không có dữ liệu"}"
                    } else {
                        Toast.makeText(this@MainActivity, "Lỗi: ${jsonResponse.getString("error")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Lỗi phân tích JSON: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this@MainActivity, "Lỗi mạng: ${error.message}", Toast.LENGTH_LONG).show()
            }) {

            override fun getBodyContentType(): String {
                return "multipart/form-data;boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"
            }

            override fun getBody(): ByteArray {
                val params = HashMap<String, String>()
                params["user_id"] = "anonymous"
                return getMultipartBody(params, byteArray)
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                return super.parseNetworkResponse(response)
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