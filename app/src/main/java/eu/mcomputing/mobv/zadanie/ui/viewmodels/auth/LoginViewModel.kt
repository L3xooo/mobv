package eu.mcomputing.mobv.zadanie.ui.viewmodels.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.data.api.models.LoginRequest
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()
    val loginSuccess = MutableLiveData<Boolean>()

    private val repository = DataRepository.getInstance(app)

    fun onLoginClicked() {
        val nameValue = username.value
        val passwordValue = password.value

        if (nameValue.isNullOrBlank() || passwordValue.isNullOrBlank()) {
            errorMessage.value = "Please fill in all fields!"
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.loginUser(LoginRequest(nameValue, passwordValue))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.access.isNotBlank()) {
                        loginSuccess.value = true
                    } else {
                        errorMessage.value = "Invalid username or password!"
                    }
                } else {
                    errorMessage.value = "Login failed: ${response.code()} ${response.message()}"
                }

            } catch (e: Exception) {
                errorMessage.value = "Login failed: ${e.message}"
            }
        }
    }

}