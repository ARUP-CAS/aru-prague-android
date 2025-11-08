package cz.visualio.archeologie.androidApp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import cz.visualio.archeologie.androidApp.databinding.ActivityMainBinding
import cz.visualio.archeologie.androidApp.viewmodels.AndroidApplicationViewModel
import cz.visualio.archeologie.shared.service.APIService
import cz.visualio.archeologie.shared.service.ApiRepository
import cz.visualio.archeologie.shared.util.getCachedOkHttpClient
import cz.visualio.archeologie.shared.viewmodels.ApplicationAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit


@ExperimentalSerializationApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val vm: AndroidApplicationViewModel by lazy { ViewModelProvider(this)[AndroidApplicationViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        ApiRepository.init(
            Retrofit.Builder()
                .client(getCachedOkHttpClient(applicationContext, 10 * 1024 * 1024L))
                .baseUrl("https://archeologie.visu.cz/api/")
                .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
                .build()
                .create(APIService::class.java)
        )

        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            vm.dispatch(ApplicationAction.LoadThematics)
            vm.dispatch(ApplicationAction.LoadLocations)
        }

        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission)
        lifecycleScope.launchWhenCreated {
            when(Peko.requestPermissionsAsync(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)){
                is PermissionResult.Granted -> recreate()
                else -> {}
            }
        }
    }
}