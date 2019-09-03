package kr.djspi.pipe01

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.*
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.naver.maps.map.util.FusedLocationSource
import kr.djspi.pipe01.util.messageDialog
import kr.djspi.pipe01.util.toast

abstract class LocationUpdate : BaseActivity() {

    /**
     * Provides access to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private lateinit var locationRequest: LocationRequest
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private var requestingLocationUpdates: Boolean? = null
    /**
     * Callback for Location events.
     */
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationManager: LocationManager
    private lateinit var locationSource: FusedLocationSource

    /**
     * Time when the location was updated represented as a String.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestingLocationUpdates = false
        requestAllPermissions(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(GPS_PROVIDER)) {
            messageDialog(1, getString(R.string.popup_location_on), false)
        }
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback()
        createLocationRequest()
    }

    /**
     * 앱 사용에 필요한 권한을 Array 로 입력 ('Manifest.permission.필요권한')
     */
    @SuppressLint("MissingPermission")
    private fun requestAllPermissions(context: Context) {
        val permissions =
            arrayOf(ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA)
        Permissions.check(
            context/*context*/,
            permissions,
            null,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    requestingLocationUpdates = true
                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
                    requestingLocationUpdates = false
                    toast("위치정보를 사용할 수 없습니다")
                }
            })/*rationale*//*options*/
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    currentLocation = it.lastLocation
                }
            }
        }
    }

    /**
     * Sets up the location request.
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     */
    private fun createLocationRequest() {
        locationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.interval = UPDATE_INTERVAL_IN_MS

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MS

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onResume() {
        super.onResume()
        currentLocation ?: startLocationUpdates()
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient
            .requestLocationUpdates(
                locationRequest, locationCallback, Looper.myLooper()
            )
    }

    override fun onPause() {
        super.onPause()
        // Remove location updates to save battery.
        stopLocationUpdates()
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private fun stopLocationUpdates() {
        if (requestingLocationUpdates!!) {
            // Recommended in applications that request frequent location updates.
            fusedLocationClient
                .removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this) { requestingLocationUpdates = false }
        }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MS: Long = 10000
        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MS = UPDATE_INTERVAL_IN_MS / 2
    }
}
