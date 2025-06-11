import com.example.myapplication.HistoryItem
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/api/history")
    fun getHistory(): Call<HistoryResponse>
}

data class HistoryResponse(
    val success: Boolean,
    val history: List<HistoryItem>
)
