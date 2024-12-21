package com.example.jetpackcompose.api

import android.util.Log
import com.example.jetpackcompose.data.ForecastData
import com.example.jetpackcompose.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.UnknownHostException

object WeatherApiService {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApi::class.java)

    interface WeatherApi {
        @GET("weather")
        suspend fun fetchWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<WeatherData>

        @GET("forecast")
        suspend fun fetchForecast(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): retrofit2.Response<ForecastData>
    }

    suspend fun fetchWeather(city: String, apiKey: String): WeatherData? {
        return try {
            withContext(Dispatchers.Default) {
                val response = api.fetchWeather(city, apiKey)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("WeatherApiService", "Failed to fetch data: ${response.code()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherApiService", "Error fetching data: ${e.message}")
            null
        }
    }


    ////////////////////////////////////

    // TODO: Methode fetchForecast implementieren, um die Wettervorhersage abzurufen.

    /**
     * Ruft die Wettervorhersage für eine bestimmte Stadt über die OpenWeather-API an
     * @param city Name der Stadt, die für die Vorhersage aberufen werden soll
     * @param apiKey API-Schlüssel zur Authentifizierung bei OpenWeather
     * @return ForecastData, enthält die Wettervorhersage oder null, falls die Anfrage fehlschlägt
     */
    suspend fun fetchForecast(city: String, apiKey: String): ForecastData? {
        return try {
            withContext(Dispatchers.IO) { // IO statt Default für Netzwerkoperationen
                val response = api.fetchForecast(city, apiKey)
                if (response.isSuccessful) {
                    response.body() // Erfolgreiche Antwort wird zurückgegeben
                } else {
                    Log.e(
                        "WeatherApiService",
                        "Failed to fetch forecast data: HTTP ${response.code()} - ${response.message()}"
                    )
                    null
                }
            }
        } catch (e: UnknownHostException) {
            // Spezifische Behandlung für Netzwerkprobleme
            Log.e("WeatherApiService", "Network error: Unable to resolve host. ${e.localizedMessage}")
            null
        } catch (e: Exception) {
            // Generische Ausnahmebehandlung
            Log.e("WeatherApiService", "Unexpected error fetching forecast data: ${e.localizedMessage}")
            null
        }
    }
    ////////////////////////////////////
}
