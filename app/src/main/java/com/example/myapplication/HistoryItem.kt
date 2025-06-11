package com.example.myapplication

data class HistoryItem(
    val image_base64: String,
    val info: Info,
    val predicted_label: String,
    val timestamp: String,
    val top3: List<TopLabel>
)

data class Info(
    val ten: String,
    val loai: String,
    val loi_ich: List<String>,
    val luu_y: String,
    val link: String,
    val thanh_phan: List<ThanhPhan>
)

data class ThanhPhan(
    val ten: String,
    val gia_tri: String
)

data class TopLabel(
    val label: String,
    val score: Double
)
