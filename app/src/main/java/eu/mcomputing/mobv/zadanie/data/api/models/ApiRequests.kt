package eu.mcomputing.mobv.zadanie.data.api.models

data class LoginRequest(
    val name: String,
    val password: String
)

data class PasswordChangeRequest(
    val old_password: String,
    val new_password: String
)

data class PasswordResetRequest(
    val email: String
)

data class RegisterRequest(
    val name: String,
    val password: String,
    val email: String
)

data class RefreshTokenRequest (
    val refresh : String= "",
)

data class GeofenceUpdateRequest (
    val lat: Double,
    val lon: Double,
    val radius: Int,
)