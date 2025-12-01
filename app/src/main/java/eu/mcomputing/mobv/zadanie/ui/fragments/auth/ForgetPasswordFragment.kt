package eu.mcomputing.mobv.zadanie.ui.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.databinding.FragmentForgetPasswordBinding
import eu.mcomputing.mobv.zadanie.ui.viewmodels.auth.ForgetPasswordViewModel

class ForgetPasswordFragment: Fragment(R.layout.fragment_forget_password)   {
    private lateinit var binding: FragmentForgetPasswordBinding
    private val viewModel: ForgetPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.backToLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_forget_password_to_login)
        }

        return binding.root;
    }
}