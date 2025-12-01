package eu.mcomputing.mobv.zadanie.ui.fragments.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.databinding.FragmentLoginBinding
import eu.mcomputing.mobv.zadanie.ui.viewmodels.auth.LoginViewModel
import eu.mcomputing.mobv.zadanie.workers.MyWorker
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.loginSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                 findNavController().navigate(R.id.action_login_to_profile)

                val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()

                WorkManager.getInstance(requireContext())
                    .enqueueUniqueWork(
                        "MyWorker",
                        ExistingWorkPolicy.KEEP,   // ✅ correct policy for one-time work
                        workRequest
                    )
                    .result
                    .addListener(
                        { Log.d("LoginFragment", "WorkManager enqueued successfully!") },
                        ContextCompat.getMainExecutor(requireContext())
                    )

            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        binding.forgetPasswordButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forget_password)
        }

        binding.registerLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        return binding.root;
    }
}