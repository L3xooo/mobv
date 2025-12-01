package eu.mcomputing.mobv.zadanie.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import eu.mcomputing.mobv.zadanie.R
import eu.mcomputing.mobv.zadanie.data.GeofenceManager
import eu.mcomputing.mobv.zadanie.data.LocationManager
import eu.mcomputing.mobv.zadanie.data.repository.DataRepository
import eu.mcomputing.mobv.zadanie.databinding.FragmentProfileBinding
import eu.mcomputing.mobv.zadanie.ui.utils.GeofenceUtils
import eu.mcomputing.mobv.zadanie.ui.viewmodels.ProfileViewModel
import eu.mcomputing.mobv.zadanie.ui.viewmodels.factory.ProfileViewModelFactory
import eu.mcomputing.mobv.zadanie.ui.widgets.NavbarLayout
import eu.mcomputing.mobv.zadanie.utils.SharedPreferencesUtil
import eu.mcomputing.mobv.zadanie.workers.MyWorker
import kotlinx.coroutines.launch
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ProfileFragment: Fragment(R.layout.fragment_profile)   {

    private val args: ProfileFragmentArgs by navArgs()
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result.all { it.value }
            if (granted) permissionsGranted() else permissionsDenied()
        }
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            changePhoto(uri)
        }
    }


    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(
            DataRepository.getInstance(requireContext()),
            GeofenceManager(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.locationSharingSwitch.isChecked = SharedPreferencesUtil.getSharingType(
            SharedPreferencesUtil.SharingType.AUTOMATIC)
        binding.manualLocationSharingSwitch.isChecked = SharedPreferencesUtil.getSharingType(
            SharedPreferencesUtil.SharingType.MANUAL)

        binding.apply {
            navbar.setActive(NavbarLayout.Tab.PROFILE)
            topNavbar.setTitle("Profile")
            changePasswordButton.setOnClickListener {
                showChangePasswordDialog()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = LocationManager(requireContext())

        val userId: String = args.userId?.takeIf { it.isNotBlank() }
            ?: SharedPreferencesUtil.userId ?: ""

        viewModel.loadUser(userId)
        mapView = binding.mapView

        subscribeToGeofenceUpdates();

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            101
        )

        if (SharedPreferencesUtil.userId != userId) {
            binding.photoButtonContainer.visibility = View.GONE
            binding.changePasswordButton.visibility = View.GONE
            binding.profileCard.visibility = View.GONE
            binding.locationSharingDescription.visibility = View.GONE
        }

        binding.updatePhotoButton.setOnClickListener {
            if (checkPermissions(ask = true)) {
                permissionsGranted()
            } else {
                permissionsDenied()
            }
        }

        binding.manualLocationSharingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.locationSharingSwitch.isChecked = false
                SharedPreferencesUtil.setSharingType(SharedPreferencesUtil.SharingType.MANUAL, true)
                locationManager.getCurrentLocation { location ->
                    location?.let {
                        viewModel.onManualSwitchButtonClick(true, it.latitude, it.longitude)
                    }
                }

                val workRequest = PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES)
                    .addTag("NOTIFICATION")
                    .build()

                WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                    "NOTIFICATION_WORK",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )

            } else {
                SharedPreferencesUtil.setSharingType(SharedPreferencesUtil.SharingType.MANUAL, false)
                viewModel.onManualSwitchButtonClick(false, null, null)
                WorkManager.getInstance(requireContext()).cancelAllWorkByTag("NOTIFICATION")
            }
        }

        binding.locationSharingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.manualLocationSharingSwitch.isChecked = false
                SharedPreferencesUtil.setSharingType(SharedPreferencesUtil.SharingType.AUTOMATIC, true)
                locationManager.getCurrentLocation { location ->
                    location?.let {
                        viewModel.onAutomaticSwitchButtonClick(isChecked, requireContext())
                    }
                }
            } else {
                SharedPreferencesUtil.setSharingType(SharedPreferencesUtil.SharingType.AUTOMATIC, false)
                viewModel.onAutomaticSwitchButtonClick(isChecked,requireContext())
            }
        }
    }

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun subscribeToGeofenceUpdates() {

        val dataRepo = DataRepository.getInstance(requireContext())
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->

            dataRepo.getLocation().observe(viewLifecycleOwner) { location ->

                if (location != null) {
                    Log.d("ProfileFragment", "Got geofence location: $location")
                    binding.mapViewWrapper.visibility = View.VISIBLE;

                    GeofenceUtils.zoomToLocation(mapView.getMapboxMap(),
                        location.lat, location.lon, 12.0)

                    style.removeStyleLayer("circle-layer-map3")
                    style.removeStyleSource("circle-source-map3")
                    GeofenceUtils.drawGeofenceCircle(
                        style = style,
                        latitude = location.lat,
                        longitude = location.lon,
                        radiusMeters = location.radius,
                        sourceId = "circle-source-map3",
                        layerId = "circle-layer-map3",
                    )

                } else {
                    Log.d("ProfileFragment", "Location null → hiding geofence")
                    binding.mapViewWrapper.visibility = View.GONE;
                }
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val oldPassword = dialogView.findViewById<EditText>(R.id.oldPassword)
        val newPassword = dialogView.findViewById<EditText>(R.id.newPassword)

        MaterialAlertDialogBuilder(requireContext(), R.style.MyAlertDialogTheme)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val oldPassword = oldPassword.text.toString()
                val newPassword = newPassword.text.toString()
                viewModel.onPasswordChangeButtonClick(oldPassword, newPassword)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun permissionsGranted() {
        openGallery()
    }

    fun permissionsDenied() {
        Toast.makeText(
            requireContext(),
            "Permissions not granted by the user.",
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun checkPermissions(ask: Boolean = false): Boolean {
        val check = allPermissionsGranted()
        if (ask && !check) {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS())
        }
        return check
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS().all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun REQUIRED_PERMISSIONS(): Array<String> {

        return if (Build.VERSION.SDK_INT < 33) {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).toTypedArray()
        } else if (Build.VERSION.SDK_INT == 33) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            ).toTypedArray()
        } else {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ).toTypedArray()
        }
    }

    fun inputStreamToFile(
        uri: Uri,
    ): File? {
        val resolver = requireContext().applicationContext.contentResolver
        resolver.openInputStream(uri).use { inputStream ->
            var orig = File(requireContext().filesDir, "photo_copied.jpg")
            if (orig.exists()) {
                orig.delete()
            }
            orig = File(requireContext().filesDir, "photo_copied.jpg")

            FileOutputStream(orig).use { fileOutputStream ->
                if (inputStream == null) {
                    return null
                }
                try {
                    inputStream.copyTo(fileOutputStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }
            }
            return orig
        }

    }
    private fun changePhoto(file: Uri) {
        inputStreamToFile(file)?.let {
            lifecycleScope.launch {
                DataRepository.getInstance(requireContext()).uploadImage(Uri.fromFile(it))
            }
        }
    }

    private fun openGallery() {
        lifecycleScope.launch {
            val mimeType = "image/jpeg"
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.SingleMimeType(
                        mimeType
                    )
                )
            )
        }
    }
}