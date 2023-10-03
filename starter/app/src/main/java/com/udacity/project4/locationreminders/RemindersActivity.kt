package com.udacity.project4.locationreminders

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.createChannel
import java.util.concurrent.TimeUnit

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var navController: NavController
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        if (FirebaseAuth.getInstance().currentUser?.displayName.isNullOrBlank().not()) {
            Log.e("tri11", FirebaseAuth.getInstance().currentUser?.displayName.orEmpty())
            navController.graph =
                navController.navInflater.inflate(R.navigation.nav_graph).apply {
                    setStartDestination(R.id.reminderListFragment)
                }
        }

        geofencingClient = LocationServices.getGeofencingClient(this)
        createChannel(this)
        setContentView(binding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (binding.navHostFragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addGeofenceForClue(reminderDataItem: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude ?: 0.0,
                reminderDataItem.longitude ?: 0.0,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this, GeofenceTransitionsJobIntentService::class.java)
            intent.action = ACTION_GEOFENCE_EVENT
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        if (ActivityCompat.checkSelfPermission(
                this@RemindersActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    Toast.makeText(
                        this@RemindersActivity,
                        "Clue Geofence added.",
                        Toast.LENGTH_SHORT
                    ).show()
                    GeofenceTransitionsJobIntentService.enqueueWork(
                        this@RemindersActivity,
                        intent
                    )
                    Log.e("tri11", geofence.requestId)
                }
                addOnFailureListener {
                    Toast.makeText(
                        this@RemindersActivity, R.string.geofences_not_added,
                        Toast.LENGTH_SHORT
                    ).show()
                    if ((it.message != null)) {
                        Log.e("tri11", it.message!!)
                    }
                }
            }
        }
    }
}
