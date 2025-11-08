package cz.visualio.archeologie.shared.service

import arrow.core.Either
import cz.visualio.archeologie.shared.model.Thematic
import cz.visualio.archeologie.shared.model.toEntity
import kotlinx.serialization.json.Json

val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

interface ApiRepository {
    val apiService: APIService

    suspend fun getThematics(language: String): Either<Throwable, List<Thematic>> = Either.catch {
            apiService.getThematics(language).thematics.map { it.toEntity() }
    }

    suspend fun getLocations(language: String) = Either.catch {
        apiService.getLocations(language).locations
    }


    companion object {
        private lateinit var obj: ApiRepository
        fun init(apiService: APIService) {
            if (!::obj.isInitialized) obj = object : ApiRepository {
                override val apiService = apiService
            }
        }

        operator fun invoke() = obj
    }
}