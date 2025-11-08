package cz.visualio.archeologie.shared.model


interface HasLatLng{
    val latitude: Double
    val longitude: Double
}

interface Entity{
    val id: Long
}

interface HasSort{
    val sort: Double
}

interface HasTitle{
    val title: String
}

interface HasText{
    val text: String
}