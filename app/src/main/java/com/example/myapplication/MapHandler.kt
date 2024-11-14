package com.example.myapplication

import android.annotation.SuppressLint
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.viewport.ViewportPlugin
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import kotlin.math.roundToInt

class MapHandler(ctx: AppCompatActivity, bind: ActivityMainBinding) {


    private val activityCompat = ctx
    private val binding = bind
    private var mapPitch: Double = 0.0
    private var mapZoom: Double = 18.0
    var isMapRotating: Boolean = true
    private lateinit var myPosition: Location
    private lateinit var viewport: ViewportPlugin
    private val fusedLocationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                myPosition = p0.lastLocation!!
                val currentSpeed = (p0.lastLocation!!.speed * 18 / 5).roundToInt()
                binding.speedView.text = currentSpeed.toString()
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun init() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // Interval set to 10 seconds
            .setIntervalMillis(5000) // Fastest updates every 5 seconds
            .build()
        LocationServices.getFusedLocationProviderClient(activityCompat)
            .requestLocationUpdates(locationRequest, fusedLocationCallback, Looper.getMainLooper())
        binding.mapView.mapboxMap.loadStyle(
            activityCompat.resources.getString(R.string.mapbox_style_light)
        ) {
            viewport = binding.mapView.viewport

            myPosition = Location("")
            myPosition.latitude = 43.213116
            myPosition.longitude = 27.916077
            myPosition.bearing = 0.0F

            binding.mapView.scalebar.enabled = false
            binding.mapView.compass.enabled = false
            binding.mapView.location.apply {
                this.locationPuck = LocationPuck2D(
                    bearingImage = ImageHolder.from(
                        com.mapbox.maps.R.drawable.mapbox_user_puck_icon,
                    ),
                    shadowImage = ImageHolder.from(
                        com.mapbox.maps.R.drawable.mapbox_user_icon_shadow
                    )
                )
                puckBearingEnabled = true
                puckBearing= PuckBearing.COURSE
                enabled = true
                pulsingEnabled = true
            }
            startUpdatingCamera()
            binding.mapView.camera.addCameraZoomChangeListener {
                mapZoom = it
            }
        }
        binding.mapView.getMapboxMap().addOnMoveListener(object : OnMoveListener {
            private val handler: Handler = Handler(Looper.getMainLooper())
            override fun onMove(detector: MoveGestureDetector): Boolean {
                return false
            }

            override fun onMoveBegin(detector: MoveGestureDetector) {
                viewport.idle()

            }

            override fun onMoveEnd(detector: MoveGestureDetector) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    startUpdatingCamera()
                }, 30000)
            }

        })


    }

    fun finish() {
        LocationServices.getFusedLocationProviderClient(activityCompat)
            .removeLocationUpdates(fusedLocationCallback)
    }

    fun startUpdatingCamera(isPaddingLeft: Boolean = false) {
        if (this::viewport.isInitialized) {


            mapPitch = if (isMapRotating) 50.00 else 0.0
            val mapPaddingTop = if (isMapRotating) 350.00 else 0.0
            val mapPaddingLeft = if (isPaddingLeft) 350.00 else 0.0
            val deviceDensity = activityCompat.resources.displayMetrics.density

            val animation = binding.mapView.viewport.makeDefaultViewportTransition(
                DefaultViewportTransitionOptions.Builder()
                    .maxDurationMs(1000)
                    .build()
            )
            val followPuckViewportState = binding.mapView.viewport.makeFollowPuckViewportState(
                FollowPuckViewportStateOptions.Builder()
                    .bearing(
                        if (isMapRotating)
                            FollowPuckViewportStateBearing.SyncWithLocationPuck
                        else
                            FollowPuckViewportStateBearing.Constant(0.0)
                    )
                    .padding(
                        EdgeInsets(
                            mapPaddingTop * deviceDensity,
                            mapPaddingLeft * deviceDensity,
                            0.0,
                            0.0
                        )
                    )
                    .pitch(mapPitch)
                    .zoom(mapZoom)
                    .build()
            )
            viewport.transitionTo(followPuckViewportState, animation)
        }
    }

    fun lastPosition(): Location {
        return myPosition
    }

    fun addMarker() {
        // TODO adding marker on map
    }
}