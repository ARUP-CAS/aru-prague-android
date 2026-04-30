package cz.visualio.sauersack.androidApp.shared.model

import cz.visualio.sauersack.androidApp.shared.Entity
import cz.visualio.sauersack.androidApp.shared.GeoJsonFeatureRes
import cz.visualio.sauersack.androidApp.shared.HasTitle
import cz.visualio.sauersack.androidApp.shared.ThematicRes

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
        logosUrl = listOf(logo1_url, logo2_url, logo3_url, logo4_url)
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
    val logosUrl: List<String?>,
) : Entity, HasTitle
