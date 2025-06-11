package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject


class SocKetActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var predictionText: TextView
    private lateinit var mSocket: Socket


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.socket)

        webView = findViewById(R.id.webview_video)
        predictionText = findViewById(R.id.text_prediction)

        // Load video stream
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("http://192.168.1.139:5000/video_feed") // thay YOUR_IP bằng IP server Flask

        // Kết nối WebSocket
        try {
            val opts = IO.Options()
            opts.forceNew = true
            opts.reconnection = true
            mSocket = IO.socket("http://192.168.1.139:5000", opts) // same IP
            mSocket.connect()


            mSocket.on("prediction_result") { args ->
                runOnUiThread {
                    val data = args[0] as JSONObject
                    if (data.has("predicted_label")) {
                        val label = data.getString("predicted_label")
                        val info = data.getJSONObject("info").toString()

                        // Get top 3 predictions
                        val top3 = data.getJSONArray("top3")
                        val top3List = StringBuilder("Top 3 Predictions\n")
                        for (i in 0 until top3.length()) {
                            val obj = top3.getJSONObject(i)
                            val lbl = obj.getString("label")
                            val score = obj.getDouble("score")
                            top3List.append("$lbl: $score%\n")
                        }
                        predictionText.text = "Dự đoán: $label\n\n$top3List"
//                        predictionText.text = "Dự đoán: $label\n$info\n\n$top3List"
                    } else if (data.has("error")) {
                        predictionText.text = "Lỗi: ${data.getString("error")}"
                    }
                }
            }

        } catch (e: Exception) {
            predictionText.text = "Lỗi kết nối socket: ${e.message}"
        }

        // Gửi yêu cầu mỗi giây để nhận frame xử lý
        val handler = Handler(Looper.getMainLooper())
        val frameRunnable = object : Runnable {
            override fun run() {
                mSocket.emit("stream_frame", JSONObject())  // gửi yêu cầu xử lý frame
                handler.postDelayed(this, 500)  // lặp lại sau 1s
            }
        }
        handler.post(frameRunnable)
    }
}
