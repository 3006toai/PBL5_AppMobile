package com.example.myapplication

data class ThanhPhan(
    val ten: String,
    val gia_tri: String
)

data class Info(
    val ten: String,
    val link: String,
    val loai: String,
    val loi_ich: List<String>,
    val luu_y: String,
    val thanh_phan: List<ThanhPhan>
)

data class HistoryItem(
    val imagebase64: String,
    val info: Info,
    val timestamp: String
)
