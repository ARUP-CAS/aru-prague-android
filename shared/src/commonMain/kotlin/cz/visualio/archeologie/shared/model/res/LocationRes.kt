package cz.visualio.archeologie.shared.model.res

import cz.visualio.archeologie.shared.model.Entity
import cz.visualio.archeologie.shared.model.HasLatLng
import cz.visualio.archeologie.shared.model.HasText
import cz.visualio.archeologie.shared.model.HasTitle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationRes(
    override val id: Long,
    override val title: String,
    override val latitude: Double,
    override val longitude: Double,
    val type: IconType,
    val address: String,
    val image: String,
    val externalLink: String?,
    val thematics: Set<Long>,
    val content: List<Container>,
    val openTime: Boolean,
    val timeOfVisit: Int,
    val availability: Availability
) : Entity, HasLatLng, HasTitle

@Suppress("EnumEntryName")
enum class Availability{
    easy, good, hard,
}

@Suppress("EnumEntryName")
enum class IconType {
    judaismus, bojiste_mlade, bojiste_stare, dum, hrad, hradiste, industrial, klaster, most, kostel, muzeum, opevneni, palac, pametihodnost, pohrebiste, pravek, rozhled, stredovek, studna, tvrz, usedlost, vesnice, vyhled, zamek
}

@Serializable
sealed class Content: HasText {

    @Serializable
    data class Text(override val text: String) : Content()

    @Serializable
    data class Model(val urlFile: String, val urlImage: String, override val text: String,
                     val sort: Long) :
        Content()

    @Serializable
    data class File(val urlIos: String, val urlAndroid: String, override val text: String,
                     val sort: Long) :
        Content()

    @Serializable
    data class Image(val url: String, override val text: String, val sort: Long) : Content()

    @Serializable
    data class Video(val urlImage: String, val urlVideo: String, override val text: String, val sort: Long) :
        Content()
}

@Serializable
sealed class Container {
    abstract val content: List<Content>

    @Serializable
    @SerialName("text")
    data class Text(override val content: List<Content.Text>) : Container()

    @Serializable
    @SerialName("models")
    data class Model(override val content: List<Content.Model>) : Container()

    @Serializable
    @SerialName("files")
    data class File(override val content: List<Content.File>) : Container()

    @Serializable
    @SerialName("images")
    data class Image(override val content: List<Content.Image>) : Container()

    @Serializable
    @SerialName("videos")
    data class Video(override val content: List<Content.Video>) : Container()

}