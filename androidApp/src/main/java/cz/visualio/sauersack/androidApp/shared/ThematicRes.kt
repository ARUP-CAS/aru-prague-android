package cz.visualio.sauersack.androidApp.shared

import kotlinx.serialization.Serializable

@Serializable
data class ThematicRes(
    override val id: Long,
    override val latitude: Double,
    override val longitude: Double,
    val image: String,
    val logo1: String,
    val logo2: String,
    val logo3: String,
    val logo4: String,
    override val title: String,
    val locations: Set<Long>,
    val author: String?,
    val professionalCooperation: String?,
    val artisticsCooperation: String?,
    val thanks: String?,
    val geoJson: GeoJsonFeatureRes?,
    val characteristics: String?,
    val logo1_url: String?,
    val logo2_url: String?,
    val logo3_url: String?,
    val logo4_url: String?,
) : Entity, HasLatLng, HasTitle


