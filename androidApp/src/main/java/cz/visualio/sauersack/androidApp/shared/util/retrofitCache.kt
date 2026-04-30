package cz.visualio.sauersack.androidApp.shared.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw      = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

 val logging = HttpLoggingInterceptor().apply{
    setLevel(HttpLoggingInterceptor.Level.BODY)
}

fun getCachedOkHttpClient(context: Context, cacheSize: Long): OkHttpClient =
    OkHttpClient.Builder()
        .cache(Cache(context.cacheDir, cacheSize))
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
            when {
                !isNetworkAvailable(context) -> request.header(
                    "Cache-Control",
                    "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 365
                )
                else -> request.header("Cache-Control", "public, max-age=" + 5)
            }.build()
                .let(chain::proceed)
        }.build()