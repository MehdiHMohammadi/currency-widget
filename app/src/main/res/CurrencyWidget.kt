import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CurrencyWidget : AppWidgetProvider() {

    private val apiKey = "FreeOoaSw7qkzYc0WZiDmzIzFq9akFRZ"

    interface CurrencyApi {
        @GET("Market/Gold_Currency.php")
        suspend fun getCurrencies(@Query("key") apiKey: String): retrofit2.Response<CurrencyResponse>
    }

    data class CurrencyResponse(val data: List<CurrencyItem>)
    data class CurrencyItem(val symbol: String, val price: String, val name: String)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://brsapi.ir/Api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(CurrencyApi::class.java)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // برای هر ویجت فعال، داده ها را بروز کن
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.widget_content, "در حال بارگذاری...")

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // اجرای درخواست API در Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getCurrencies(apiKey)
                if (response.isSuccessful) {
                    val currencies = response.body()?.data ?: emptyList()
                    val usd = currencies.find { it.symbol == "USD" }?.price ?: "N/A"
                    val eur = currencies.find { it.symbol == "EUR" }?.price ?: "N/A"
                    val aed = currencies.find { it.symbol == "AED" }?.price ?: "N/A"
                    val tryPrice = currencies.find { it.symbol == "TRY" }?.price ?: "N/A"

                    val text = """
                        دلار: $usd
                        یورو: $eur
                        درهم: $aed
                        لیر ترکیه: $tryPrice
                    """.trimIndent()

                    withContext(Dispatchers.Main) {
                        val updatedViews = RemoteViews(context.packageName, R.layout.widget_layout)
                        updatedViews.setTextViewText(R.id.widget_content, text)
                        appWidgetManager.updateAppWidget(appWidgetId, updatedViews)
                    }
                } else {
                    updateError(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                updateError(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateError(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.widget_content, "خطا در دریافت داده‌ها")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
