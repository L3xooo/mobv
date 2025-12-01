package eu.mcomputing.mobv.zadanie.ui.fragments

import MapViewModelFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.ui.viewmodels.MapViewModel
import eu.mcomputing.mobv.zadanie.ui.widgets.NavbarLayout
import eu.mcomputing.mobv.zadanie.ui.widgets.TopNavbarLayout
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import eu.mcomputing.mobv.zadanie.data.db.entities.Location
import eu.mcomputing.mobv.zadanie.data.db.entities.User
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.ui.utils.GeofenceUtils

class MapFragment: Fragment(R.layout.fragment_map) {
    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(DataRepository.getInstance(requireContext()))
    }

    private var mapView: MapView? = null
    private var pointAnnotationManager: PointAnnotationManager? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<NavbarLayout>(R.id.navbar).setActive(NavbarLayout.Tab.MAP)
        view.findViewById<TopNavbarLayout>(R.id.topNavbar).setTitle("Map")

        view.findViewById<MaterialButton>(R.id.enableLocationButton).setOnClickListener {
            findNavController().navigate(R.id.action_map_to_profile)
        }

        val locationWarning = view.findViewById<ConstraintLayout>(R.id.locationWarning)
        mapView = view.findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.Companion.MAPBOX_STREETS)

        if (SharedPreferencesUtil.isSharingEnabled()) {
            mapView?.visibility = View.VISIBLE
            locationWarning.visibility = View.GONE
            viewModel.loadPeople()
        } else {
            mapView?.visibility = View.GONE
            locationWarning.visibility = View.VISIBLE
        }

        viewModel.people.observe(viewLifecycleOwner) { (users, location) ->
            Log.d("MapFragment", "$users")
            Log.d("MapFragment", "$location")
            showMapMarkers(users, location)
        }
    }

    private fun showMapMarkers(users: List<User>?, location: Location?) {
        Log.d("MapFragmentGeofence"," Triggered rerendering map")
        val mapboxMap = mapView?.getMapboxMap() ?: return

        val locationComponent = mapView?.location
        locationComponent?.updateSettings {
            enabled = true
            pulsingEnabled = true
        }

        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->

            val annotationApi = mapView!!.annotations
            if (pointAnnotationManager == null) {
                pointAnnotationManager = annotationApi.createPointAnnotationManager()
            } else {
                pointAnnotationManager?.deleteAll()
            }

            if (location != null) {
                GeofenceUtils.zoomToLocation(
                    mapboxMap,
                    location.lat,
                    location.lon,
                    12.0
                )

                // ✅ Draw your geofence circle
                GeofenceUtils.drawGeofenceCircle(
                    style = style,
                    latitude = location.lat,
                    longitude = location.lon,
                    radiusMeters = location.radius,
                    sourceId = "circle-source-map",
                    layerId = "circle-layer-map",
                )
            }
            // Zoom to the user’s last known location


            if (users != null && location != null) {
                for (user in users) {
                    if (user.uid != SharedPreferencesUtil.userId) {
                        val randomPoint = GeofenceUtils.generateRandomPointInRadius(
                            Point.fromLngLat(location.lon, location.lat),
                            location.radius
                        )

                        GeofenceUtils.createUserMarker(
                            requireContext(),
                            pointAnnotationManager,
                            randomPoint,
                            user.uid,
                            imageUrl = null,
                            R.drawable.profile_avatar_placeholder
                        )
                    }
                }
            }
        }
    }

}