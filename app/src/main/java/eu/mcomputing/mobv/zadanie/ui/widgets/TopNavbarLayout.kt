package eu.mcomputing.mobv.zadanie.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TopNavbarLayout(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    private val toolbarTitle: TextView
    private val logoutButton: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.top_navbar_layout, this, true)
        toolbarTitle = findViewById(R.id.toolbar_title)
        logoutButton = findViewById(R.id.logoutButton)

        logoutButton.setOnClickListener { onLogoutButtonClick() }
    }

    fun setTitle(title: String) {
        toolbarTitle.text = title
    }

    private fun onLogoutButtonClick() {
        CoroutineScope(Dispatchers.Main).launch {
            DataRepository.getInstance(context).logout()
            SharedPreferencesUtil.logout()

            findNavController().navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }
}
