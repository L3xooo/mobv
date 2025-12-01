package eu.mcomputing.mobv.zadanie.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.google.android.material.button.MaterialButton
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.ui.widgets.NavbarLayout
import eu.mcomputing.mobv.zadanie.ui.widgets.TopNavbarLayout
import eu.mcomputing.mobv.zadanie.ui.adapters.UserAdapter
import eu.mcomputing.mobv.zadanie.ui.viewmodels.PeopleViewModel
import eu.mcomputing.mobv.zadanie.ui.viewmodels.factory.PeopleViewModelFactory
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil

class PeopleFragment : Fragment(R.layout.fragment_people) {

    private lateinit var adapter: UserAdapter

    private val viewModel: PeopleViewModel by viewModels {
        PeopleViewModelFactory(DataRepository.getInstance(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.enableLocationButton).setOnClickListener {
            findNavController().navigate(R.id.action_people_to_profile)
        }

        val locationWarning = view.findViewById<ConstraintLayout>(R.id.locationWarning)
        val navbar = view.findViewById<NavbarLayout>(R.id.navbar)
        val topNavbar = view.findViewById<TopNavbarLayout>(R.id.topNavbar)
        navbar.setActive(NavbarLayout.Tab.PEOPLE)
        topNavbar.setTitle("People")


        adapter = UserAdapter { user ->
            Log.d("PeopleFragment", "User clicked: ${user.name} ${user.uid}")
            val action = PeopleFragmentDirections.actionPeopleToProfile(user.uid)
            findNavController().navigate(action)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter


        if (SharedPreferencesUtil.isSharingEnabled()) {
            Log.d("PeopleFramgds", "called")
            recyclerView.visibility = View.VISIBLE
            locationWarning.visibility = View.GONE
            viewModel.loadPeople()
        } else {
            recyclerView.visibility = View.GONE
            locationWarning.visibility = View.VISIBLE
        }

        viewModel.people.observe(viewLifecycleOwner) { response ->
            response?.let {
                adapter.setUsers(it)
            }
        }
    }
}
