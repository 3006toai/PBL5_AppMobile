package com.example.myapplication

import HistoryAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class HistoryActivity : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.history, container, false)
        recyclerView = view.findViewById(R.id.history_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchHistoryData()
        return view
    }

    private fun fetchHistoryData() {
        val request = Request.Builder()
            .url("http://172.20.10.6:5000/api/history") // 🔁 Đổi IP nếu Flask server bạn khác
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Xử lý lỗi nếu cần
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    val gson = Gson()
                    val type = object : TypeToken<List<HistoryItem>>() {}.type
                    val historyList: List<HistoryItem> = gson.fromJson(json, type)

                    activity?.runOnUiThread {
                        adapter = HistoryAdapter(historyList)
                        recyclerView.adapter = adapter
                    }
                }
            }
        })
    }
}
