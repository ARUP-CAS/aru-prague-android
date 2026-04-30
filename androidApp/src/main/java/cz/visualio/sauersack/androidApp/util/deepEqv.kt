package cz.visualio.sauersack.androidApp.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import cz.visualio.sauersack.androidApp.fragments.getARGBLong
import cz.visualio.sauersack.androidApp.shared.GeoJsonFeatureRes
import cz.visualio.sauersack.androidApp.shared.viewmodels.ApplicationState

fun <K, V> Map<K, V>.deepEqv(b: Map<K, V>, compare: (V, V) -> Boolean): Boolean =
    this.size == b.size && this.all { (key, value) -> b[key]?.let { compare(it, value) } ?: false }


fun ApplicationState.toGeoJson(): Map<String, GeoJsonFeature> =
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