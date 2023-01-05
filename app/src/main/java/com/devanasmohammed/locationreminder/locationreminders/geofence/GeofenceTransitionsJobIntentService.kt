package com.devanasmohammed.locationreminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.devanasmohammed.locationreminder.locationreminders.data.ReminderDataSource
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import com.devanasmohammed.locationreminder.locationreminders.data.dto.Result
import com.devanasmohammed.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.devanasmohammed.locationreminder.utils.Constants.Companion.ACTION_GEOFENCE_EVENT
import com.devanasmohammed.locationreminder.utils.sendNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    /**
     * handle the geofencing transition events and send
     * a notification to the user when he enters the geofence area
     */
    override fun onHandleWork(intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e("onHandleWork", geofencingEvent.errorCode.toString())
                    return
                }

                if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    sendNotification(geofencingEvent.triggeringGeofences!!)
                }
            }
        }

    }

    /**
     * get the request id of the current geofence
     */
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        for (triggeringGeofence in triggeringGeofences) {
            val requestId = triggeringGeofence.requestId
            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()
            //Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }

        }
    }

}
