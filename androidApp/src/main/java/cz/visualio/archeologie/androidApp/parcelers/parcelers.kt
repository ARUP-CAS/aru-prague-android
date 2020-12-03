package cz.visualio.archeologie.androidApp.parcelers

import android.os.Parcel
import android.os.Parcelable
import cz.visualio.archeologie.shared.model.Thematic
import cz.visualio.archeologie.shared.model.res.*
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler


@Parcelize
@TypeParceler<Thematic, ThematicParceler>
data class ThematicParcelable(val thematic: Thematic) : Parcelable


object ThematicParceler : Parceler<Thematic> {
    override fun create(parcel: Parcel): Thematic {
        val logos = mutableListOf<String>()
        val locationIds = longArrayOf()
        parcel.readStringList(logos)
        parcel.readLongArray(locationIds)
        return Thematic(
            id = parcel.readLong(),
            logos = logos,
            title = parcel.readString()!!,
            locationIds = locationIds.toSet(),
            author = parcel.readString(),
            professionalCooperation = parcel.readString(),
            artisticCooperation = parcel.readString(),
            thanks = parcel.readString(),
            imageUrl = parcel.readString()!!,
            geoJson = null,
            characteristics = parcel.readString(),
        )
    }

    override fun Thematic.write(parcel: Parcel, flags: Int) {
        parcel.writeList(logos)
        parcel.writeArray(locationIds.toTypedArray())
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(professionalCooperation)
        parcel.writeString(artisticCooperation)
        parcel.writeString(thanks)
        parcel.writeString(imageUrl)
        parcel.writeString(characteristics)

    }
}


object GeoJsonFeaturePropertiesParceler : Parceler<GeoJsonFeatureProperties> {
    override fun create(parcel: Parcel): GeoJsonFeatureProperties {
        return GeoJsonFeatureProperties(
            stroke = parcel.readString()!!,
            strokeWidth = parcel.readFloat(),
            strokeOpacity = parcel.readFloat(),
            fill = parcel.readString()!!,
            fillOpacity = parcel.readFloat(),
            topicId = parcel.readLong(),
        )
    }

    override fun GeoJsonFeatureProperties.write(parcel: Parcel, flags: Int) {
        parcel.writeString(stroke)
        parcel.writeFloat(strokeWidth)
        parcel.writeFloat(strokeOpacity)
        parcel.writeString(fill)
        parcel.writeFloat(fillOpacity)
        parcel.writeLong(topicId)
    }
}

@Parcelize
@TypeParceler<LocationRes, LocationResParceler>
data class LocationParcelable(
    val value: LocationRes
) : Parcelable

object LocationResParceler : Parceler<LocationRes> {
    override fun create(parcel: Parcel): LocationRes {
        val thematicIds = longArrayOf()
        parcel.readLongArray(thematicIds)
        return LocationRes(
            id = parcel.readLong(),
            title = parcel.readString()!!,
            latitude = parcel.readDouble(),
            longitude = parcel.readDouble(),
            address = parcel.readString()!!,
            externalLink = parcel.readString(),
            thematics = thematicIds.toSet(),
            content = parcel.readParcelable<ContentWrapper>(this::class.java.classLoader)!!.value,
            type = parcel.readString()!!.let { IconType.valueOf(it) },
            image = parcel.readString()!!,
            openTime = parcel.readInt() == 1,
            availability = Availability.values()[parcel.readInt()],
            timeOfVisit = parcel.readInt()
        )
    }

    override fun LocationRes.write(parcel: Parcel, flags: Int) {
        parcel.writeLongArray(thematics.toLongArray())
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(address)
        parcel.writeString(externalLink)
        parcel.writeParcelable(ContentWrapper(content), flags)
        parcel.writeString(type.name)
        parcel.writeString(image)
        parcel.writeInt(if(openTime) 1 else 0)
        parcel.writeInt(availability.ordinal)
        parcel.writeInt(timeOfVisit)
    }
}

@Parcelize
@TypeParceler<Container, ContainerParceler>
data class ContentWrapper(val value: List<Container>) : Parcelable

@Parcelize
enum class Type : Parcelable {
    TEXT, IMAGE, VIDEO, MODEL, FILE
}

@Parcelize
@TypeParceler<Content.Text, ContentTextParceler>
data class ContentTextWrapper(val value: List<Content.Text>) : Parcelable

object ContentTextParceler : Parceler<Content.Text> {
    override fun create(parcel: Parcel): Content.Text =
        Content.Text(
            text = parcel.readString()!!
        )


    override fun Content.Text.write(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
    }
}

@Parcelize
@TypeParceler<Content.Image, ContentImageParceler>
data class ContentImageWrapper(val value: List<Content.Image>) : Parcelable

object ContentImageParceler : Parceler<Content.Image> {
    override fun create(parcel: Parcel): Content.Image = Content.Image(
        text = parcel.readString()!!,
        sort = parcel.readLong(),
        url = parcel.readString()!!
    )

    override fun Content.Image.write(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeLong(sort)
        parcel.writeString(url)
    }
}

@Parcelize
@TypeParceler<Content.Video, ContentVideoParceler>
data class ContentVideoWrapper(val value: List<Content.Video>) : Parcelable

object ContentVideoParceler : Parceler<Content.Video> {
    override fun create(parcel: Parcel): Content.Video = Content.Video(
        text = parcel.readString()!!,
        sort = parcel.readLong(),
        urlImage = parcel.readString()!!,
        urlVideo = parcel.readString()!!
    )

    override fun Content.Video.write(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeLong(sort)
        parcel.writeString(urlImage)
        parcel.writeString(urlVideo)
    }
}

@Parcelize
@TypeParceler<Content.Model, ContentModelParceler>
data class ContentModelWrapper(val value: List<Content.Model>) : Parcelable

object ContentModelParceler : Parceler<Content.Model> {
    override fun create(parcel: Parcel): Content.Model = Content.Model(
        text = parcel.readString()!!,
        urlImage = parcel.readString()!!,
        sort = parcel.readLong(),
        urlFile = parcel.readString()!!
    )

    override fun Content.Model.write(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeString(urlImage)
        parcel.writeLong(sort)
        parcel.writeString(urlFile)
    }
}

@Parcelize
@TypeParceler<Content.File, ContentFileParceler>
data class ContentFileWrapper(val value: List<Content.File>) : Parcelable

object ContentFileParceler : Parceler<Content.File> {
    override fun create(parcel: Parcel): Content.File  =Content.File(
        text = parcel.readString()!!,
        sort = parcel.readLong(),
        urlIos = parcel.readString()!!,
        urlAndroid = parcel.readString()!!
    )

    override fun Content.File.write(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeLong(sort)
        parcel.writeString(urlIos)
        parcel.writeString(urlAndroid)
    }
}

object ContainerParceler : Parceler<Container> {
    override fun create(parcel: Parcel): Container =

        when (parcel.readParcelable<Type>(this::class.java.classLoader)!!) {
            Type.TEXT -> parcel.readParcelable<ContentTextWrapper>(this::class.java.classLoader)!!
                .value
                .let(Container::Text)
            Type.IMAGE -> parcel.readParcelable<ContentImageWrapper>(this::class.java.classLoader)!!
                .value
                .let(Container::Image)
            Type.VIDEO -> parcel.readParcelable<ContentVideoWrapper>(this::class.java.classLoader)!!
                .value
                .let(Container::Video)
            Type.MODEL -> parcel.readParcelable<ContentModelWrapper>(this::class.java.classLoader)!!
                .value
                .let(Container::Model)
            Type.FILE -> parcel.readParcelable<ContentFileWrapper>(this::class.java.classLoader)!!
                .value
                .let(Container::File)
        }

    override fun Container.write(parcel: Parcel, flags: Int) {
        when (this) {
            is Container.Text -> {
                parcel.writeParcelable(Type.TEXT, flags)
                ContentTextWrapper(content)
            }
            is Container.Model -> {
                parcel.writeParcelable(Type.MODEL, flags)
                ContentModelWrapper(content)
            }
            is Container.Image -> {
                parcel.writeParcelable(Type.IMAGE, flags)
                ContentImageWrapper(content)
            }
            is Container.Video -> {
                parcel.writeParcelable(Type.VIDEO, flags)
                ContentVideoWrapper(content)
            }
            is Container.File -> {
                parcel.writeParcelable(Type.FILE, flags)
                ContentFileWrapper(content)
            }
        }.let { parcel.writeParcelable(it, flags) }
    }
}