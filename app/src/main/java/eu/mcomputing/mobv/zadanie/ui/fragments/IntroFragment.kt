package eu.mcomputing.mobv.zadanie.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import eu.mcomputing.mobv.zadanie.R

class IntroFragment : Fragment(R.layout.fragment_intro) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_intro_to_login)
        }
        registerButton.setOnClickListener {
             findNavController().navigate(R.id.action_intro_to_register)
        }
    }
}