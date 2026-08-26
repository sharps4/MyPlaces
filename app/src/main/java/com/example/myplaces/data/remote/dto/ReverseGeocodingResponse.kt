package com.example.myplaces.data.remote.dto
data class ReverseGeocodingResponse(
    val features: List<Feature> = emptyList()
) {
    val firstLabel: String? get() = features.firstOrNull()?.properties?.label
}

data class Feature(
    val properties: Properties = Properties()
)

data class Properties(
    val label: String? = null,
    val postcode: String? = null,
    val city: String? = null
)
