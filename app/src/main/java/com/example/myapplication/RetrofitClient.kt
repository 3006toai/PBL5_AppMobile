import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/" // Đảm bảo có dấu "/" ở cuối

    val instance: ApiService by lazy {
        // Tạo OkHttpClient với interceptor để hỗ trợ UTF-8 và timeout
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Accept-Charset", "utf-8")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS) // Tăng timeout nếu server chậm
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Khởi tạo Retrofit với client tùy chỉnh
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

