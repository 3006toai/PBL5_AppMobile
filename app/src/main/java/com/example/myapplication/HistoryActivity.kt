package com.example.myapplication

import android.content.Intent
import com.google.gson.Gson
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import com.example.myapplication.R
import com.example.myapplication.HistoryAdapter
import com.example.myapplication.HistoryItem
import com.example.myapplication.HistoryResponse
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject


class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        val homeButton = findViewById<LinearLayout>(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@HistoryActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Nếu bạn không muốn quay lại HistoryActivity khi bấm nút Back
        }


        recyclerView = findViewById(R.id.history_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        fetchHistory()



    }

    private fun fetchHistory() {
        val url = "http://10.0.2.2:5000/api/history"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                Log.d("VolleyResponse", "Raw response: $response")

                try {
                    val gson = Gson()
                    val historyResponse = gson.fromJson(response.toString(), HistoryResponse::class.java)

                    if (historyResponse.success) {
                        historyList.clear()
                        historyList.addAll(historyResponse.history)
                        historyAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Lấy dữ liệu thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("Volley", "JSON parse error: ${e.message}")
                    Toast.makeText(this, "Lỗi đọc dữ liệu", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                if (error.networkResponse != null) {
                    val data = String(error.networkResponse.data)
                    Log.e("Volley", "Lỗi mạng: ${error.networkResponse.statusCode} - $data")
                } else {
                    Log.e("Volley", "Lỗi kết nối: ${error.message}")
                }
                Toast.makeText(this, "Lỗi kết nối: ${error.message}", Toast.LENGTH_LONG).show()
            })

        Volley.newRequestQueue(this).add(request)
    }

}