package cz.visualio.archeologie.shared.model.res

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GeoJsonFeatureRes(
    val type: String,
    val properties: GeoJsonFeatureProperties,
    val geometry: GeoJsonGeometryRes,
)


@Serializable
data class GeoJsonGeometryRes(
    val type: String,
    val coordinates: List<List<List<Double>>>
)
@Serializable
data class GeoJsonFeatureProperties(
    val stroke: String, // color
    @SerialName("stroke-width") val strokeWidth: Float,
    @SerialName("stroke-opacity") val strokeOpacity: Float,
    val fill: String,
    @SerialName("fill-opacity") val fillOpacity: Float,
    @SerialName("topic-id") val topicId: Long
)