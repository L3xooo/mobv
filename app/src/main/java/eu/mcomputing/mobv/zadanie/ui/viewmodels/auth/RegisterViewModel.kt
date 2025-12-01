package eu.mcomputing.mobv.zadanie.ui.viewmodels.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.data.api.models.RegisterRequest
import eu.mcomputing.mobv.zadanie.data.api.models.AuthResponse
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterViewModel(app: Application) : AndroidViewModel(app) {

    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val registrationSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    private val repository = DataRepository.getInstance(app)

    fun onRegisterClicked() {
        val nameValue = username.value
        val emailValue = email.value
        val passwordValue = password.value
        if (nameValue.isNullOrBlank() || emailValue.isNullOrBlank() || passwordValue.isNullOrBlank()) {
            errorMessage.value = "Please fill in all the fields!"
            return
        }

        viewModelScope.launch {
            try {
                val response: Response<AuthResponse> = repository.registerUser(
                    RegisterRequest(
                        name = nameValue,
                        password = passwordValue,
                        email = emailValue
                    )
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        when (body.uid) {
                            "-1" -> errorMessage.value = "Username already exists, please choose another"
                            "-2" -> errorMessage.value = "Email already exists, please choose another"
                            else -> {
                                SharedPreferencesUtil.saveTokens(
                                    body.access,
                                    body.refresh,
                                    body.uid
                                )
                                registrationSuccess.value = true
                            }
                        }
                    } else {
                        errorMessage.value = "Error: Empty response from server"
                    }
                } else {
                    errorMessage.value = "Registration failed: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Registration error: ${e.message}"
            }
        }
    }
}
