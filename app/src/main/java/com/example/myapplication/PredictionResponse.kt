data class PredictionResponse(
    val success: Boolean, // Trạng thái thành công của API
    val predictions: List<Prediction>, // Danh sách dự đoán top 3
    val plant_info: Map<String, String>, // Thông tin chi tiết từ Firestore (bao gồm "Thành phần", "Vitamin C", v.v.)
    val image_url: String // URL của ảnh đã upload
)

data class Prediction(
    val label: String, // Nhãn dự đoán (ví dụ: "apple")
    val score: Float // Độ tin cậy (phần trăm)
)
