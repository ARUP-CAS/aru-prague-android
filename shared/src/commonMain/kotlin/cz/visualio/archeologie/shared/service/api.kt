package cz.visualio.archeologie.shared.service

import cz.visualio.archeologie.shared.model.res.LocationRes
import cz.visualio.archeologie.shared.model.res.ThematicRes
import kotlinx.serialization.Serializable

expect interface APIService {
    suspend fun getThematics(language: String): ThematicsCallResponse
    suspend fun getLocations(language: String): LocationsCallResponse
//    suspend fun getGeoJson(): GeoJsonRes
}


interface CallResponse {
    val status: String
    val statusCode: Int
}

@Serializable
data class ThematicsCallResponse(
    override val status: String,
    override val statusCode: Int,
    val thematics: List<ThematicRes>
) : CallResponse


@Serializable
data class LocationsCallResponse(
    override val status: String,
    override val statusCode: Int,
    val locale: String,
    val locations: List<LocationRes>
) : CallResponse

