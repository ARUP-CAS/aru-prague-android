package cz.visualio.archeologie.androidApp.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.awaitMap
import cz.visualio.archeologie.androidApp.R
import cz.visualio.archeologie.androidApp.adapters.ViewPager2Adapter
import cz.visualio.archeologie.androidApp.databinding.FragmentLoactionMapBinding
import cz.visualio.archeologie.androidApp.parcelers.LocationParcelable
import cz.visualio.archeologie.androidApp.util.LocationClusterRenderer
import cz.visualio.archeologie.androidApp.util.MarginPageTransformer
import cz.visualio.archeologie.androidApp.viewmodels.AndroidApplicationViewModel
import cz.visualio.archeologie.shared.model.Thematic
import cz.visualio.archeologie.shared.model.res.Availability
import cz.visualio.archeologie.shared.model.res.LocationRes
import cz.visualio.archeologie.shared.viewmodels.ApplicationAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.ExperimentalSerializationApi

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalSerializationApi
class LocationMapFragment : Fragment() {
    private val vm: AndroidApplicationViewModel by lazy { ViewModelProvider(requireActivity())[AndroidApplicationViewModel::class.java] }

    private val semaphore = Semaphore(1, 1)

    private lateinit var bottomSheet: BottomSheetBehavior<CardView>
    private val addressTextView: TextView
        get() = binding.textView3
    private val gpsTextView: TextView
        get() = binding.textView4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thematic = LocationMapFragmentArgs.fromBundle(requireArguments()).thematic.thematic
    }

    private fun onLocationSelected(location: LocationRes) {
        lifecycleScope.launchWhenResumed {
            vm.dispatch(ApplicationAction.SetLocationBotomSheetExpanded(true))
            if (location.id != vm.state.activeLocation?.id)
                vm.dispatch(ApplicationAction.SetActiveLocation(location))
        }
    }


    override fun onResume() {
        super.onResume()

        val vpAdapter = ViewPager2Adapter(
            fragment = this@LocationMapFragment,
            createFragment = LocationItemFragment.Companion::newInstance,
            onItemClick = {
                onLocationSelected(it)
                true
            }
        )

        vp.adapter = vpAdapter


        vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val newItem = vpAdapter.getItem(position)

                lifecycleScope.launchWhenResumed {
                    vm.dispatch(ApplicationAction.SetActiveLocation(newItem))
                }
            }
        })

        lifecycleScope.launchWhenResumed {
            vm.flow.collect {
                val newState = if (it.locationBottomSheetExpanded)
                    BottomSheetBehavior.STATE_EXPANDED
                else BottomSheetBehavior.STATE_COLLAPSED

                if (newState != bottomSheet.state)
                    bottomSheet.state = newState
            }
        }

        lifecycleScope.launchWhenResumed {
            vm.flow
                .mapNotNull { it.filteredLocations }
                .collect { vpAdapter.update(it) }
        }

        lifecycleScope.launchWhenResumed {
            vm.flow.mapNotNull { it.activeLocation }
                .collect {
                        showOnWebButton.isVisible = it.externalLink != null
                }
        }

        vpAdapter.update(vm.state.filteredLocations)
        when (val active = vm.state.activeLocation) {
            is LocationRes -> vpAdapter.getPositionById(active.id, LocationRes::id)
                .let { vp.setCurrentItem(it, false) }
        }

        lifecycleScope.launchWhenResumed {
            var last: LocationRes? = null
            vm.flow.map { it.activeLocation }
                .collect {
                    if (it != last) {
                        last = it
                        it?.id?.let { id -> vpAdapter.getPositionById(id, LocationRes::id) }
                            ?.let { if (vp.currentItem != it) vp.currentItem = it }

                        it?.let { fillView(it) }
                    }
                }
        }


        lifecycleScope.launchWhenResumed {
            semaphore.acquire()

            lifecycleScope.launch {
                vm.flow.mapLatest { state ->
                    state.filteredLocations
                        .map {
                            LocationClusterItem(
                                value = it,
                                isActive = it == state.activeLocation,
                            )
                        }
                }
                    .collect {
                        clusterManager.clearItems()
                        clusterManager.addItems(it)
                        clusterManager.cluster()
                    }
            }

        }
    }

    private  val ivDifficulty: ImageView
        get() = binding.imageView7
    private val ivTime: ImageView
    get() = binding.imageView8
    private val ivOpeningHours: ImageView
        get() = binding.imageView6

    private fun fillView(it: LocationRes) {
        binding.textView.text = it.title
        addressTextView.text = it.address
        gpsTextView.text = "${it.latitude} ${it.longitude}"


        ivOpeningHours.isVisible = it.openTime
        ivDifficulty.setImageResource(it.difficultyIcon)

        ivTime.isVisible = it.timeOfVisit != 0
        if (it.timeOfVisit != 0) ivTime.setImageResource(it.timeIcon)
    }

    private val LocationRes.timeIcon
        get() = when (timeOfVisit) {
            15 -> R.drawable.visit_duration_15
            30 -> R.drawable.visit_duration_30
            45 -> R.drawable.visit_duration_45
            60 -> R.drawable.visit_duration_60
            else -> throw IllegalStateException("timeOfVisit cannot be: $timeOfVisit")
        }

    private val LocationRes.difficultyIcon
        get() = when (availability) {
            Availability.easy -> R.drawable.difficulty_easy
            Availability.good -> R.drawable.difficulty_medium
            Availability.hard -> R.drawable.difficulty_hard
        }

    private lateinit var googleMap: GoogleMap
    private lateinit var binding: FragmentLoactionMapBinding


    @FlowPreview
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentLoactionMapBinding.inflate(inflater, container, false).apply {
        binding = this
        backButton.setOnClickListener { activity?.onBackPressed() }
    }.root

    private lateinit var thematic: Thematic
    private val vp: ViewPager2
        get() = binding.VPThematic


    private lateinit var clusterManager: ClusterManager<LocationClusterItem>
    private  val showOnWebButton: Button
        get() = binding.button4
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp.apply {
                offscreenPageLimit = 3
                setPageTransformer(MarginPageTransformer(context.getCarouselMargin()))
            }


        val bottomSheetCard = binding.bottomSheet
        bottomSheetCard.setOnClickListener { }
        bottomSheet = BottomSheetBehavior.from(bottomSheetCard)

        val detailButton: Button = binding.button
        val navigateButton: Button = binding.button2


        detailButton.setOnClickListener {
            vm.state.activeLocation?.let {
                LocationMapFragmentDirections.actionLoactionMapFragmentToCarouselFragment(
                    LocationParcelable(it)
                )
            }?.let {
                findNavController().navigate(it)
            }
        }

        navigateButton.setOnClickListener {
            vm.state.activeLocation
                ?.let { "geo:0,0?q=${it.latitude}, ${it.longitude}(${it.title})" }
                ?.let(Uri::parse)
                ?.let { Intent(Intent.ACTION_VIEW, it) }
                ?.let(::startActivity)
        }

        showOnWebButton.setOnClickListener {
            vm.state.activeLocation
                ?.externalLink
                ?.let(Uri::parse)
                ?.let { Intent(Intent.ACTION_VIEW, it) }
                ?.let(::startActivity)
        }

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                lifecycleScope.launchWhenResumed {
                    val isCollapsed = newState == BottomSheetBehavior.STATE_COLLAPSED
                    vm.dispatch(ApplicationAction.SetLocationBotomSheetExpanded(!isCollapsed))
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        lifecycleScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()

            if (!::clusterManager.isInitialized) {
                clusterManager = ClusterManager<LocationClusterItem>(context, googleMap)
                clusterManager.renderer =
                    LocationClusterRenderer(requireContext(), googleMap, clusterManager)
            }

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

            googleMap.setOnMarkerClickListener(clusterManager)
            googleMap.setOnCameraIdleListener(clusterManager)
            googleMap.setOnInfoWindowClickListener(clusterManager)

            clusterManager.setOnClusterClickListener {
                it.items
                    .map(LocationClusterItem::getPosition)
                    .fold(LatLngBounds.Builder(), LatLngBounds.Builder::include)
                    .build()
                    .let { CameraUpdateFactory.newLatLngBounds(it, 80) }
                    .let(googleMap::animateCamera)

                true
            }

            clusterManager.setOnClusterItemClickListener {
                onLocationSelected(it.value)
                true
            }

            semaphore.release()

//            lifecycleScope.launchWhenResumed {
            vm.state.filteredLocations
                .map { LatLng(it.latitude, it.longitude) }
                .fold(LatLngBounds.Builder(), LatLngBounds.Builder::include)
                .build()
                .let { CameraUpdateFactory.newLatLngBounds(it, 80) }
                .let(googleMap::moveCamera)

            googleMap.awaitLoaded()


            var first = true
            var last: LocationRes? = null
            vm.flow.map { it.activeLocation }
                .collect {
                    when {
                        first -> first = false
                        it != last -> {
                            last = it
                            it?.let {
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    ), 18f
                                )
                                    .let {
                                        googleMap.animateCamera(
                                            it,
                                            object : GoogleMap.CancelableCallback {
                                                override fun onFinish() {
                                                    if (vm.state.locationBottomSheetExpanded)
                                                        googleMap.animateCamera(
                                                            CameraUpdateFactory.scrollBy(
                                                                0f,
                                                                250f
                                                            )
                                                        )
                                                }

                                                override fun onCancel() {}
                                            }
                                        )
                                    }

                            }

                        }
                    }
                }


//            }
        }
    }
}

data class LocationClusterItem(val value: LocationRes, val isActive: Boolean) : ClusterItem {
    private val latLng = LatLng(value.latitude, value.longitude)

    override fun getPosition(): LatLng = latLng

    override fun getTitle(): String = value.title

    override fun getSnippet(): String? = null
}

