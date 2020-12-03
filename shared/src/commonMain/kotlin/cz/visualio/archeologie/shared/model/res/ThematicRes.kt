package cz.visualio.archeologie.shared.model.res

import cz.visualio.archeologie.shared.model.Entity
import cz.visualio.archeologie.shared.model.HasLatLng
import cz.visualio.archeologie.shared.model.HasTitle
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
) : Entity, HasLatLng, HasTitle


