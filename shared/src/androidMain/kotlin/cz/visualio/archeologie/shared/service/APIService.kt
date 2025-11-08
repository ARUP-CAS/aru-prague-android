package cz.visualio.archeologie.shared.service

import retrofit2.http.GET
import retrofit2.http.Header

actual interface APIService {
    @GET("thematics")
    actual suspend fun getThematics(@Header("Accept-Language") language: String): ThematicsCallResponse

    @GET("location")
    actual suspend fun getLocations(@Header("Accept-Language") language: String): LocationsCallResponse

}