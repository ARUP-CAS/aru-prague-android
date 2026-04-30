package cz.visualio.sauersack.androidApp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import cz.visualio.sauersack.androidApp.databinding.ActivityMainBinding
import cz.visualio.sauersack.androidApp.shared.service.APIService
import cz.visualio.sauersack.androidApp.shared.service.ApiRepository
import cz.visualio.sauersack.androidApp.shared.util.getCachedOkHttpClient
import cz.visualio.sauersack.androidApp.shared.viewmodels.ApplicationAction
import cz.visualio.sauersack.androidApp.viewmodels.AndroidApplicationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit


@ExperimentalSerializationApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val vm: AndroidApplicationViewModel by lazy { ViewModelProvider(this)[AndroidApplicationViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {

        ApiRepository.init(
            Retrofit.Builder()
                .client(getCachedOkHttpClient(applicationContext, 10 * 1024 * 1024L))
                .baseUrl("https://sauersack.visu.cz/api-v2/")
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build()
                )
                .addConverterFactory(
                    Json {
                        ignoreUnknownKeys = true
                    }.asConverterFactory("application/json".toMediaTypeOrNull()!!)
                )
                .build()
                .create(APIService::class.java)
        )

        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 35) {
            val container = binding.fragment

            ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    0,
                    systemBars.top,
                    0,
                    systemBars.bottom
                )
                insets
            }
        }


        lifecycleScope.launchWhenCreated {
            vm.dispatch(ApplicationAction.LoadThematics)
            vm.dispatch(ApplicationAction.LoadLocations)
        }

        val hasPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission)
            lifecycleScope.launchWhenCreated {
                when (Peko.requestPermissionsAsync(this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                    is PermissionResult.Granted -> recreate()
                    else -> {
                    }
                }
            }
    }
}