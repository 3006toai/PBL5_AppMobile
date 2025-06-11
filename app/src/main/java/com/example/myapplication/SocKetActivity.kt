package com.example.myapplication

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class SocKetActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var resultText: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private var lastSendTime = 0L // Để giới hạn gửi 1 ảnh mỗi giây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.socket)

        previewView = findViewById(R.id.previewView)
        resultText = findViewById(R.id.resultText)

        requestPermissionAndStartCamera()

        // Kết nối socket
        SocketManager.initSocket()
        SocketManager.connect()

        SocketManager.socket.on("prediction_result") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                runOnUiThread {
                    if (data.has("error")) {
                        resultText.text = "Lỗi: ${data.getString("error")}"
                    } else {
                        val label = data.getString("predicted_label")
                        val info = data.getJSONObject("info").optString("info", "")
                        resultText.text = "Dự đoán: $label\nThông tin: $info"
                    }
                }
            }
        }
    }

    private fun requestPermissionAndStartCamera() {
        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) startCamera()
            else resultText.text = "Yêu cầu quyền Camera!"
        }
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor, { imageProxy ->
                        processImageFrame(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("Camera", "Không thể khởi động camera", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageFrame(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastSendTime >= 1000) {
            val bitmap = imageProxy.toBitmap()
            sendFrameToServer(bitmap)
            lastSendTime = now
        }
        imageProxy.close()
    }

    private fun sendFrameToServer(bitmap: Bitmap) {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
        val base64Image = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)

        val json = JSONObject()
        json.put("image", base64Image)

        SocketManager.socket.emit("stream_frame", json)
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.disconnect()
    }
}
