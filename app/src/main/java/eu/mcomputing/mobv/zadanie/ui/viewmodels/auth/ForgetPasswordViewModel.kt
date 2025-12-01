package eu.mcomputing.mobv.zadanie.ui.viewmodels.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.data.api.models.PasswordResetRequest
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import kotlinx.coroutines.launch

class ForgetPasswordViewModel(app: Application) : AndroidViewModel(app) {
    val email = MutableLiveData<String>()
    val resetSuccess = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    private val repository = DataRepository.getInstance(app)

    fun onResetClick() {
        val emailValue = email.value
        Log.d("email", "$emailValue")
        if (emailValue.isNullOrBlank()) {
            errorMessage.value = "Fill up the email field!"
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.resetPassword(
                    PasswordResetRequest(email = emailValue)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (body.status == "failure") {
                            errorMessage.value = body.message ?: "Unknown error"
                        } else {
                            resetSuccess.value = true
                        }
                    } else {
                        errorMessage.value = "Empty response from server"
                    }
                } else {
                    errorMessage.value = "Server error: ${response.code()} ${response.message()}"
                }

            } catch (e: Exception) {
                errorMessage.value = "Error resetting password: ${e.message}"
            }
        }
    }
}