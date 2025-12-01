package eu.mcomputing.mobv.zadanie.data.api.models

data class AuthResponse(
    val uid: String,
    val access: String,
    val refresh: String
)

data class StatusResponse(
    val status: String,
    val message : String?,
)

data class UserResponse(
    val id: String,
    val name: String,
    val photo: String
)

data class ProfilePhotoResponse(
    val id: Int,
    val name: String,
    val photo: String
)

data class MeGeofenceResponse(
    val uid: String,
    val lat: String,
    val lon: String,
    val radius: String,
)

data class GeofenceUpdateResponse(
    val success: String
)

data class GeofenceResponse(
    val uid: String,
    val radius: String,
    val updated: String,
    val name: String,
    val photo: String
)

data class GeofenceListResponse (
    val list: List<GeofenceResponse>,
    val me: MeGeofenceResponse
)
