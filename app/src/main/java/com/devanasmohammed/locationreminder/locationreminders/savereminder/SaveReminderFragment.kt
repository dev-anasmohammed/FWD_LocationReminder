package com.devanasmohammed.locationreminder.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.devanasmohammed.locationreminder.BuildConfig
import com.devanasmohammed.locationreminder.R
import com.devanasmohammed.locationreminder.base.BaseFragment
import com.devanasmohammed.locationreminder.base.NavigationCommand
import com.devanasmohammed.locationreminder.databinding.FragmentSaveReminderBinding
import com.devanasmohammed.locationreminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.devanasmohammed.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.devanasmohammed.locationreminder.utils.Constants.Companion.ACTION_GEOFENCE_EVENT
import com.devanasmohammed.locationreminder.utils.Constants.Companion.LOCATION_RC
import com.devanasmohammed.locationreminder.utils.LocationPermissionHelper
import com.devanasmohammed.locationreminder.utils.setDisplayHomeAsUpEnabled
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

@Suppress("DEPRECATION")
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var activityResultLauncherPermissions: ActivityResultLauncher<Array<String>>
    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getBroadcast(requireContext(), 0, intent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
        } else {
            getBroadcast(requireContext(), 0, intent, FLAG_UPDATE_CURRENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel

        //init geofence client
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        activityResultLauncherPermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (result.all { result -> result.value }) {
                    //granted
                    checkLocationSettingsAndStartGeofence()
                } else {
                    //not granted
                    Snackbar.make(
                        requireView(),
                        R.string.permission_denied_explanation, Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Displays App settings screen.
                            startActivity(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }.show()
                }
            }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminderDataItem =
                ReminderDataItem(title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                //check for permission before adding geoFencing request then save to local
                LocationPermissionHelper(
                    requireActivity(),
                    requireView(),
                    activityResultLauncherPermissions,
                    true
                ).checkPermissionThenDoMethod {
                    checkLocationSettingsAndStartGeofence()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /**
     * Checking Location Settings before adding geofence request
     */
    private fun checkLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        //location request and location request builder
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        //get settings client by location services
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        LOCATION_RC
                    )
                } catch (e: Exception) {
                    Log.d(
                        "Location settings",
                        "Error getting location settings ${e.message.toString()}"
                    )
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.ok) {
                    checkLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceForRemainder()
            }
        }
    }

    /**
     * Build the geofence using the geofence builder and make a geofence request
     */
    @SuppressLint("MissingPermission")
    private fun addGeofenceForRemainder() {
        val currentGeofenceData = reminderDataItem

        val geofence = Geofence.Builder()
            .setRequestId(currentGeofenceData.id)
            .setCircularRegion(
                currentGeofenceData.latitude!!,
                currentGeofenceData.longitude!!,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to add Geofence", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_RC) {
            if (resultCode == Activity.RESULT_OK) {
                addGeofenceForRemainder()
            } else {
                checkLocationSettingsAndStartGeofence(false)
            }
        }

    }
}
