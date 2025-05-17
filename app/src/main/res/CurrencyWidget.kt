

interface CurrencyApi {
    @GET("Market/Gold_Currency.php")
    suspend fun getCurrencies(@Query("key") apiKey: String): Response<CurrencyResponse>
}

data class CurrencyResponse(
    val data: List<CurrencyItem>
)

data class CurrencyItem(
    val symbol: String,
    val price: String,
    val name: String
)

// در ویجت یا در یک سرویس بروزرسانی ویجت
val retrofit = Retrofit.Builder()
    .baseUrl("https://brsapi.ir/Api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(CurrencyApi::class.java)

CoroutineScope(Dispatchers.IO).launch {
    val response = api.getCurrencies("FreeOoaSw7qkzYc0WZiDmzIzFq9akFRZ")
    if (response.isSuccessful) {
        val currencies = response.body()?.data ?: emptyList()
        val usdPrice = currencies.find { it.symbol == "USD" }?.price ?: "N/A"
        val eurPrice = currencies.find { it.symbol == "EUR" }?.price ?: "N/A"
        val aedPrice = currencies.find { it.symbol == "AED" }?.price ?: "N/A"
        val tryPrice = currencies.find { it.symbol == "TRY" }?.price ?: "N/A"
    }
}
