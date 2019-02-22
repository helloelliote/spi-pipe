package kr.djspi.pipe01;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.map.util.FusedLocationSource;

import static android.location.LocationManager.GPS_PROVIDER;

public abstract class LocationUpdate extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long UPDATE_INTERVAL_IN_MS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MS = UPDATE_INTERVAL_IN_MS / 2;
    // Keys for storing activity state in the Bundle.
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private static final String KEY_LOCATION = "location";
    /**
     * Provides access to the Location Settings API.
     */
    private static LocationManager locationManager;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient locationProviderClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest locationRequest;
    /**
     * Creates a callback for receiving location events.
     */
    private final LocationCallback locationCallback = new LocationCallback() {
        /**
         * This is the callback that is triggered when the
         * FusedLocationClient updates your location.
         * @param locationResult The result containing the device location.
         */
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            currentLocation = locationResult.getLastLocation();
            onLocationUpdate(currentLocation);
        }
    };
    FusedLocationSource locationSource;

    /**
     * Time when the location was updated represented as a String.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Update values using attr stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        locationSource.activate(location -> {
        });
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationRequest();
    }

    /**
     * Updates fields based on attr stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of requestLocationUpdates from the Bundle.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                requestLocationUpdates
                        = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of currentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that currentLocation
                // is not null.
                currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
        }
    }

    abstract void onLocationUpdate(Location location);

    /**
     * Sets up the location request.
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void isLocationEnabled() {
        if (!locationManager.isProviderEnabled(GPS_PROVIDER)) {
            showMessagePopup(1, getString(R.string.popup_location_on));
        }
    }

    @Override
    public void onResume() {
        isLocationEnabled();
        if (requestLocationUpdates || currentLocation == null) startLocationUpdates();
        super.onResume();
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!requestLocationUpdates) return;
        // Recommended in applications that request frequent location updates.
        locationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, task -> requestLocationUpdates = false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    /**
     * Stores activity attr in the Bundle.
     */
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, currentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }
}
