package cz.visualio.archeologie.shared.model

import cz.visualio.archeologie.shared.model.res.GeoJsonFeatureRes
import cz.visualio.archeologie.shared.model.res.ThematicRes

fun ThematicRes.toEntity() =
    Thematic(
        id = id,
        logos = listOf(logo1, logo2, logo3, logo4).filter(String::isNotBlank),
        title = title,
        locationIds = locations,
        author = author,
        professionalCooperation = professionalCooperation,
        artisticCooperation = artisticsCooperation,
        thanks = thanks,
        geoJson = geoJson,
        imageUrl = image,
        characteristics = characteristics,
    )

data class Thematic(
    override val id: Long,
    val logos: List<String>,
    override val title: String,
    val locationIds: Set<Long>,
    val author: String?,
    val professionalCooperation: String?,
    val artisticCooperation: String?,
    val thanks: String?,
    val imageUrl: String,
    val geoJson: GeoJsonFeatureRes?,
    val characteristics: String?,
) : Entity, HasTitle
