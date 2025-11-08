package cz.visualio.archeologie.androidApp.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics.DENSITY_DEVICE_STABLE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import arrow.core.getOrElse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import com.google.maps.android.ktx.awaitMap
import cz.visualio.archeologie.androidApp.R
import cz.visualio.archeologie.androidApp.adapters.ViewPager2Adapter
import cz.visualio.archeologie.androidApp.databinding.FragmentMapBinding
import cz.visualio.archeologie.androidApp.parcelers.ThematicParcelable
import cz.visualio.archeologie.androidApp.util.MarginPageTransformer
import cz.visualio.archeologie.androidApp.util.debounce
import cz.visualio.archeologie.androidApp.viewmodels.AndroidApplicationViewModel
import cz.visualio.archeologie.shared.model.Thematic
import cz.visualio.archeologie.shared.model.res.GeoJsonFeatureRes
import cz.visualio.archeologie.shared.viewmodels.ApplicationAction
import cz.visualio.archeologie.shared.viewmodels.ApplicationState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt
import kotlin.system.exitProcess


class MapFragment : Fragment() {

    private val vm: AndroidApplicationViewModel by lazy { ViewModelProvider(requireActivity())[AndroidApplicationViewModel::class.java] }

    private val semaphore = Semaphore(1, 1)

    private val currentlyDisplayedFeatureIds: MutableSet<String> = mutableSetOf()

    private lateinit var googleMap: GoogleMap
    private lateinit var bottomSheet: BottomSheetBehavior<CardView>
    private lateinit var currentLayer: GeoJsonLayer
    private lateinit var vpAdapter: ViewPager2Adapter<Thematic>

    private fun onThematicSelected(thematic: Thematic) {
        lifecycleScope.launchWhenResumed {
            vm.dispatch(ApplicationAction.SetThematicBotomSheetExpanded(true))
            if (thematic.id != vm.state.activeThematic?.id)
                vm.dispatch(ApplicationAction.SetActiveThematic(thematic))

        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenResumed {
            var last = emptyList<Thematic>()
            vm.flow
                .mapNotNull {
                    if (last != it.filteredThematics) {
                        last = it.filteredThematics
                        it.filteredThematics
                    } else null
                }
                .collect { vpAdapter.update(it) }
        }


        lifecycleScope.launchWhenResumed {
            vm.flow.collect {
                val newState = if (it.thematicBottomSheetExpanded)
                    BottomSheetBehavior.STATE_EXPANDED
                else BottomSheetBehavior.STATE_COLLAPSED

                if (newState != bottomSheet.state)
                    bottomSheet.state = newState
            }
        }

        vpAdapter.update(vm.state.filteredThematics)
        when (val active = vm.state.activeThematic) {
            is Thematic -> {
                vp.setCurrentItem(vpAdapter.getPositionById(active.id, Thematic::id), false)
            }
        }

        lifecycleScope.launchWhenResumed {
            var last: Thematic? = null
            vm.flow.map { it.activeThematic }
                .collect {
                    if (it != last) {
                        last = it
                        it?.id?.let { id -> vpAdapter.getPositionById(id, Thematic::id) }
                            ?.let { if (vp.currentItem != it) vp.currentItem = it }

                        binding.textView.text = it?.title
                        binding.textView5.text = it?.characteristics
                    }
                }
        }


        lifecycleScope.launchWhenResumed {
            semaphore.acquire()

            lifecycleScope.launch {
                var last = emptyMap<String, GeoJsonFeature>()
                vm.flow.mapNotNull {
                    val new = it.toGeoJson()

                    if (!last.deepEqv(new) { a, b ->
                            a.id == b.id && a.polygonStyle.strokeColor == b.polygonStyle.strokeColor && a.polygonStyle.strokeWidth == b.polygonStyle.strokeWidth
                        }) {
                        last = new
                        new
                    } else null
                }
                    .collect { updateGeoJson(it) }
            }

        }
    }

    private fun updateGeoJson(it: Map<String, GeoJsonFeature>) {
        val toRemove = currentlyDisplayedFeatureIds - it.keys
        toRemove.onEach(currentlyDisplayedFeatureIds::remove)

        val features = currentLayer.features.toList()
        features.onEach(currentLayer::removeFeature)

        it.onEach { (id, value) ->
            currentLayer.addFeature(value)
            currentlyDisplayedFeatureIds.add(id)
        }
    }

    @FlowPreview
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentMapBinding.inflate(inflater, container, false).apply {
        binding = this
        lifecycleScope.launchWhenCreated {
            OnQueryTextListener { vm.dispatch(ApplicationAction.SetFilterQuery(it)) }
                .let(searchBar::setOnQueryTextListener)
        }
    }.root


    @Suppress("FunctionName")
    private fun OnQueryTextListener(search: suspend (String) -> Unit): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            val debouncedSearch = search.debounce(1000, lifecycleScope)
            override fun onQueryTextSubmit(query: String?): Boolean {
                lifecycleScope.launchWhenResumed { search(query.orEmpty()) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                lifecycleScope.launchWhenResumed { debouncedSearch(newText.orEmpty()) }
                return true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity
            ?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner,
                object : OnBackPressedCallback(
                    true) {
                    var lastPressed = 0L
                    val delay = 2000L
                    override fun handleOnBackPressed() {
                        val current = System.currentTimeMillis()

                        if (current - lastPressed < delay) exitProcess(0)
                        else Toast.makeText(context,
                            getString(R.string.exitPrompt),
                            Toast.LENGTH_SHORT).show()

                        lastPressed = current
                    }
                })

    }

    private val vp: ViewPager2
    get() = binding.VPThematic

    private var first: Boolean = true

    private lateinit var binding: FragmentMapBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetCard: CardView = binding.bottomSheet
        bottomSheetCard.setOnClickListener { }
        bottomSheet = BottomSheetBehavior.from(bottomSheetCard)

        val creditsButton: Button = binding.button2
        creditsButton.setOnClickListener {
            vm.state.activeThematic?.let {
                MapFragmentDirections.actionMapFragmentToCreditsFragment(ThematicParcelable(it))
            }?.let {
                lifecycleScope.launchWhenResumed {
                    vm.dispatch(ApplicationAction.SetActiveLocation(null))
                    vm.dispatch(ApplicationAction.SetLocationBotomSheetExpanded(false))
                }
                findNavController().navigate(it)
            }
        }

        val navigateButton: Button = binding.button
        navigateButton.setOnClickListener {
            lifecycleScope.launchWhenResumed {
                vm.dispatch(ApplicationAction.SetActiveLocation(null))
                vm.dispatch(ApplicationAction.SetLocationBotomSheetExpanded(false))
            }
            vm.state.activeThematic?.let {
                MapFragmentDirections.actionMapFragmentToLoactionMapFragment(ThematicParcelable(it))
            }?.let { findNavController().navigate(it) }
        }


        vpAdapter = ViewPager2Adapter(
            fragment = this@MapFragment,
            createFragment = ThematicItemFragment.Companion::newInstance,
            onItemClick = {
                onThematicSelected(it)
                true
            }
        )


            vp.apply {
                offscreenPageLimit = 3
                setPageTransformer(MarginPageTransformer(context.getCarouselMargin()))
            }
        vp.adapter = vpAdapter

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                lifecycleScope.launchWhenResumed {
                    val isCollapsed = newState == BottomSheetBehavior.STATE_COLLAPSED
                    vm.dispatch(ApplicationAction.SetThematicBotomSheetExpanded(!isCollapsed))
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val newItem = vpAdapter.getItem(position)

                lifecycleScope.launchWhenResumed {
                    vm.dispatch(ApplicationAction.SetActiveThematic(newItem))

                }
            }
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        lifecycleScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),
                R.raw.map_style))

            listOf(
                LatLng(51.055556, 14.314722),
                LatLng(48.5525, 14.333056),
                LatLng(50.251944, 12.091389),
                LatLng(49.550278, 18.858889),
            ).fold(LatLngBounds.Builder(), LatLngBounds.Builder::include)
                .build()
                .let(googleMap::setLatLngBoundsForCameraTarget)


            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            googleMap.isMyLocationEnabled = hasPermission
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            val fab: FloatingActionButton = binding.gpsButton
            fab.isVisible = hasPermission
            if (hasPermission)
                fab.setOnClickListener {
                    val loc = googleMap.myLocation
                    CameraUpdateFactory.newLatLng(LatLng(loc.latitude, loc.longitude))
                        .let(googleMap::animateCamera)
                }

            if (!::currentLayer.isInitialized) {
                currentLayer = GeoJsonLayer(
                    googleMap,
                    JSONObject("""{"type": "FeatureCollection", "features":[]}""")
                )

                currentLayer.addLayerToMap()
                currentLayer.setOnFeatureClickListener { feature: Feature ->
                    val id = feature.id.toLong()
                    vm.state.thematicSM.select()
                        .getOrElse { null }
                        ?.find { it.id == id }
                        ?.let(::onThematicSelected)
                }
            }

            semaphore.release()

            lifecycleScope.launchWhenResumed {
                googleMap.awaitLoaded()

                var last: Thematic? = null
                vm.flow.map { it.activeThematic }
                    .collect {
                        when {
                            first -> {
                                vm.state.latLngBoundary?.let {
                                    CameraUpdateFactory.newLatLngBounds(it, 100)
                                }?.let(googleMap::moveCamera)
                                    ?.let { first = false }
                            }
                            it != last -> {
                                last = it
                                it?.geoJson?.let {
                                    it.geometry.coordinates.flatMap {
                                        it.map { (lng, lat) -> LatLng(lat, lng) }
                                    }
                                        .fold(LatLngBounds.Builder(), LatLngBounds.Builder::include)
                                        .build()
                                        .let {
                                            CameraUpdateFactory
                                                .newLatLngBounds(it, 100)
                                        }
                                        .let {
                                            googleMap.animateCamera(it)
                                        }
                                }
                            }
                        }
                    }
            }
        }
    }


}

fun Context.getCarouselMargin(): Int {
    val scaleFactor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources.displayMetrics.densityDpi.toDouble() / DENSITY_DEVICE_STABLE
        else 1.0
    return (-450 / scaleFactor).roundToInt()
}

fun getARGBLong(opacity: Float, rgbHex: String): Long =
    ((opacity * 255).roundToInt().toString(16) + rgbHex.replace("#", ""))
        .toLong(16)


private fun ApplicationState.toGeoJson(): Map<String, GeoJsonFeature> =
    filteredThematics.mapNotNull {
        when (val geoJson = it.geoJson) {
            is GeoJsonFeatureRes -> it.id to geoJson
            else -> null
        }
    }
        .map { (id, it) ->
            val geo = it.geometry.coordinates.map {
                it.map { (lat, lng) -> LatLng(lng, lat) }
            }

            val bounds = LatLngBounds.builder()
                .apply { geo.map { (latLng) -> include(latLng) } }
                .build()
            val feature = GeoJsonFeature(
                GeoJsonPolygon(geo),
                id.toString(),
                hashMapOf(),
                bounds,
            )

            feature.polygonStyle = GeoJsonPolygonStyle().apply {
                fillColor = getARGBLong(
                    it.properties.fillOpacity,
                    it.properties.fill
                ).toInt()
                strokeWidth = it.properties.strokeWidth
                strokeColor =
                    if (feature.id == activeThematic?.id?.toString())
                        getARGBLong(1f, "#000000").toInt()
                    else
                        getARGBLong(
                            it.properties.strokeOpacity,
                            it.properties.stroke
                        ).toInt()
            }

            feature
        }.associateBy(Feature::getId)


suspend fun GoogleMap.awaitLoaded() = suspendCoroutine<Unit> {
    setOnMapLoadedCallback { it.resume(Unit) }
}

private val ApplicationState.latLngBoundary
    get() = thematicSM.select()
        .getOrElse { null }
        ?.takeIf { it.isNotEmpty() }
        ?.mapNotNull(Thematic::geoJson)
        ?.flatMap { it.geometry.coordinates }
        ?.flatten()
        ?.map { (lng, lat) -> LatLng(lat, lng) }
        ?.fold(LatLngBounds.builder(), LatLngBounds.Builder::include)
        ?.build()


private fun <K, V> Map<K, V>.deepEqv(b: Map<K, V>, compare: (V, V) -> Boolean): Boolean =
    this.size == b.size && this.all { (key, value) -> b[key]?.let { compare(it, value) } ?: false }
