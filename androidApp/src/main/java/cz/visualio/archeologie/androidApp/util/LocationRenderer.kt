package cz.visualio.archeologie.androidApp.util

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import cz.visualio.archeologie.androidApp.R
import cz.visualio.archeologie.androidApp.fragments.LocationClusterItem
import cz.visualio.archeologie.shared.model.res.IconType

class LocationClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<LocationClusterItem>
)
    : DefaultClusterRenderer<LocationClusterItem>(context, map, clusterManager) {

    private val icons: MutableMap<IconType, BitmapDescriptor> = mutableMapOf()
    private val activeIcons: MutableMap<IconType, BitmapDescriptor> = mutableMapOf()

    init {
        minClusterSize = 2
    }

    private fun getClusterIconResource(isActive: Boolean, type: IconType): Int =
        if(isActive)
            when (type) {
                IconType.judaismus -> R.drawable.judaismus_selected
                IconType.bojiste_mlade -> R.drawable.bojiste_mlade_selected
                IconType.bojiste_stare -> R.drawable.bojiste_stare_selected
                IconType.dum -> R.drawable.dum_selected
                IconType.hrad -> R.drawable.hrad_selected
                IconType.hradiste -> R.drawable.hradiste_selected
                IconType.industrial -> R.drawable.industrial_selected
                IconType.klaster -> R.drawable.klaster_selected
                IconType.most -> R.drawable.most_selected
                IconType.kostel -> R.drawable.kostel_selected
                IconType.muzeum -> R.drawable.muzeum_selected
                IconType.opevneni -> R.drawable.opevneni_selected
                IconType.palac -> R.drawable.palac_selected
                IconType.pametihodnost -> R.drawable.pametihodnost_selected
                IconType.pohrebiste -> R.drawable.pohrebiste_selected
                IconType.pravek -> R.drawable.pravek_selected
                IconType.rozhled -> R.drawable.rozhled_selected
                IconType.stredovek -> R.drawable.stredovek_selected
                IconType.studna -> R.drawable.studna_selected
                IconType.tvrz -> R.drawable.tvrz_selected
                IconType.usedlost -> R.drawable.usedlost_selected
                IconType.vesnice -> R.drawable.vesnice_selected
                IconType.vyhled -> R.drawable.vyhled_selected
                IconType.zamek -> R.drawable.zamek_selected
            }
    else
        when (type) {
            IconType.judaismus -> R.drawable.judaismus_not_selected
            IconType.bojiste_mlade -> R.drawable.bojiste_mlade_not_selected
            IconType.bojiste_stare -> R.drawable.bojiste_stare_not_selected
            IconType.dum -> R.drawable.dum_not_selected
            IconType.hrad -> R.drawable.hrad_not_selected
            IconType.hradiste -> R.drawable.hradiste_not_selected
            IconType.industrial -> R.drawable.industrial_not_selected
            IconType.klaster -> R.drawable.klaster_not_selected
            IconType.most -> R.drawable.most_not_selected
            IconType.kostel -> R.drawable.kostel_not_selected
            IconType.muzeum -> R.drawable.muzeum_not_selected
            IconType.opevneni -> R.drawable.opevneni_not_selected
            IconType.palac -> R.drawable.palac_not_selected
            IconType.pametihodnost -> R.drawable.pametihodnost
            IconType.pohrebiste -> R.drawable.pohrebiste
            IconType.rozhled -> R.drawable.rozhled
            IconType.pravek -> R.drawable.pravek_not_selected
            IconType.stredovek -> R.drawable.stredovek_not_selected
            IconType.studna -> R.drawable.studna_not_selected
            IconType.tvrz -> R.drawable.tvrz_not_selected
            IconType.usedlost -> R.drawable.usedlost_not_selected
            IconType.vesnice -> R.drawable.vesnice_not_selected
            IconType.vyhled -> R.drawable.vyhled_not_selected
            IconType.zamek -> R.drawable.zamek_not_selected
        }


    override fun onBeforeClusterItemRendered(
        item: LocationClusterItem,
        markerOptions: MarkerOptions
    ) {

        val cache = if(item.isActive)
            activeIcons
        else icons

        cache.getOrPut(item.value.type) {
            getClusterIconResource(item.isActive, item.value.type)
                .let(BitmapDescriptorFactory::fromResource)
        }.let(markerOptions::icon)

        markerOptions.anchor(0.5f, 0.5f)

        super.onBeforeClusterItemRendered(item, markerOptions)
    }

}