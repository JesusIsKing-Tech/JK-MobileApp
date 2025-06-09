import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

object RetrofitClient {
    const val BASE_URL = "http://jesusking.ddns.net/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    fun getInstance(okHttpClient: OkHttpClient): Retrofit {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, CustomDateTypeAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(
                okHttpClient.newBuilder()
                    .build()
            )
            .build()
    }
}
