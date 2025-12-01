package eu.mcomputing.mobv.zadanie.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import eu.mcomputing.mobv.zadanie.R

class NavbarLayout(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    private val mapIconBtn: ImageButton
    private val listIconBtn: ImageButton
    private val profileIconBtn: ImageButton
    private val mapLabel: TextView
    private val listLabel: TextView
    private val profileLabel: TextView


    init {
        LayoutInflater.from(context).inflate(R.layout.navbar_layout, this, true)

        mapIconBtn = findViewById(R.id.mapIconButton)
        listIconBtn = findViewById(R.id.listIconButton)
        profileIconBtn = findViewById(R.id.profileIconButton)

        mapLabel = findViewById(R.id.mapIconButtonLabel)
        listLabel = findViewById(R.id.listIconButtonLabel)
        profileLabel = findViewById(R.id.profileIconButtonLabel)
        setupNavigation()
    }

    private fun setupNavigation() {
        mapIconBtn.setOnClickListener {
            findNavController().navigate(R.id.mapFragment)
            setActive(Tab.MAP)
        }

        listIconBtn.setOnClickListener {
            findNavController().navigate(R.id.peopleFragment)
            setActive(Tab.PEOPLE)
        }

        profileIconBtn.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
            setActive(Tab.PROFILE)
        }
    }

    fun setActive(tab: Tab) {
        mapIconBtn.isSelected = false
        listIconBtn.isSelected = false
        profileIconBtn.isSelected = false
        mapLabel.isSelected = false
        listLabel.isSelected = false
        profileLabel.isSelected = false

        when (tab) {
            Tab.MAP -> {
                mapIconBtn.isSelected = true
                mapLabel.isSelected = true
            }
            Tab.PEOPLE -> {
                listIconBtn.isSelected = true
                listLabel.isSelected = true
            }
            Tab.PROFILE -> {
                profileIconBtn.isSelected = true
                profileLabel.isSelected = true
            }
        }
    }

    enum class Tab { MAP, PEOPLE, PROFILE }
}