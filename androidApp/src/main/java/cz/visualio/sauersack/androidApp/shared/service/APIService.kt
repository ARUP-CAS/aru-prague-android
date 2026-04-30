package cz.visualio.sauersack.androidApp.shared.service

import cz.visualio.sauersack.androidApp.shared.LocationsCallResponse
import cz.visualio.sauersack.androidApp.shared.ThematicsCallResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface APIService {
    @GET("thematics")
    suspend fun getThematics(@Header("Accept-Language") language: String): ThematicsCallResponse

    @GET("locations")
    suspend fun getLocations(@Header("Accept-Language") language: String): LocationsCallResponse

}