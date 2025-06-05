package com.example.myapplication

data class HistoryResponse(
    val history: List<HistoryItem>,
    val success: Boolean
)

data class HistoryItem(
    val id: String,
    val image_url: String,
    val info: FruitInfo,
    val top3: List<Prediction>,
    val timestamp: String,
    val name: String
)

data class FruitInfo(
    val ten: String
)

data class Prediction(
    val label: String,
    val score: Float
)