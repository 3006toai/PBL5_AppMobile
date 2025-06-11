package com.example.myapplication

import HistoryAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        recyclerView = findViewById(R.id.history_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(emptyList()) // Set empty adapter first
        recyclerView.adapter = adapter

        val homeButton = findViewById<LinearLayout>(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: close HistoryActivity
        }
        val realTimeButton = findViewById<LinearLayout>(R.id.real_time_button)
        realTimeButton.setOnClickListener {
            val intent = Intent(this, SocKetActivity::class.java)
            startActivity(intent)
            finish() // Optional: close HistoryActivity
        }
        val cameraButton = findViewById<LinearLayout>(R.id.camera_button)
        realTimeButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish() // Optional: close HistoryActivity
        }

        fetchHistory()
    }


    private fun fetchHistory() {
        val url = "http://192.168.1.139:5000/api/history"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HistoryActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val json = JSONObject(body ?: "{}")

                if (json.getBoolean("success")) {
                    val historyJson = json.getJSONArray("history")
                    val historyItems = mutableListOf<HistoryItem>()

                    for (i in 0 until historyJson.length()) {
                        val itemObj = historyJson.getJSONObject(i)

                        val infoObj = itemObj.getJSONObject("info")
                        val loiIch = jsonArrayToList(infoObj.getJSONArray("loi_ich"))
                        val thanhPhanList = jsonArrayToListThanhPhan(infoObj.getJSONArray("thanh_phan"))

                        val info = Info(
                            ten = infoObj.getString("ten"),
                            loai = infoObj.getString("loai"),
                            loi_ich = loiIch,
                            luu_y = infoObj.getString("luu_y"),
                            link = infoObj.getString("link"),
                            thanh_phan = thanhPhanList
                        )

                        val top3Json = itemObj.getJSONArray("top3")
                        val top3 = mutableListOf<TopLabel>()
                        for (j in 0 until top3Json.length()) {
                            val topObj = top3Json.getJSONObject(j)
                            top3.add(
                                TopLabel(
                                    label = topObj.getString("label"),
                                    score = topObj.getDouble("score")
                                )
                            )
                        }

                        historyItems.add(
                            HistoryItem(
                                image_base64 = itemObj.getString("image_base64"),
                                info = info,
                                predicted_label = itemObj.getString("predicted_label"),
                                timestamp = itemObj.getString("timestamp"),
                                top3 = top3
                            )
                        )
                    }

                    runOnUiThread {
                        adapter = HistoryAdapter(historyItems)
                        recyclerView.adapter = adapter
                    }
                }
            }
        })
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    private fun jsonArrayToListThanhPhan(jsonArray: JSONArray): List<ThanhPhan> {
        val list = mutableListOf<ThanhPhan>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(ThanhPhan(obj.getString("ten"), obj.getString("gia_tri")))
        }
        return list
    }

}
