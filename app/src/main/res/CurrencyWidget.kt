class CurrencyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.widget_content, "در حال دریافت داده...")

        CoroutineScope(Dispatchers.IO).launch {
            val result = fetchCurrencyData()
            withContext(Dispatchers.Main) {
                views.setTextViewText(R.id.widget_content, result)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private suspend fun fetchCurrencyData(): String {
        return try {
            val apiKey = "FreeOoaSw7qkzYc0WZiDmzIzFq9akFRZ"
            val url = URL("https://brsapi.ir/Api/Market/Gold_Currency.php?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            val selected = listOf("usd", "eur", "aed", "gbp", "cad", "try", "cny", "jpy", "aud", "chf")
            val builder = StringBuilder()
            for (code in selected) {
                val item = json.getJSONObject(code)
                val price = item.getString("price")
                builder.append("$code: $price\n")
            }
            builder.toString()
        } catch (e: Exception) {
            "خطا در دریافت اطلاعات"
        }
    }
}
