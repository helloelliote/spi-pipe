package kr.djspi.pipe01;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.location.LocationManager.GPS_PROVIDER;

public abstract class LocationUpdate extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MS = 10000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MS = UPDATE_INTERVAL_IN_MS / 2;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient fusedLocationClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest locationRequest;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private static Boolean requestingLocationUpdates;
    /**
     * Callback for Location events.
     */
    private static LocationCallback locationCallback;

    private static LocationManager locationManager;
    FusedLocationSource locationSource;

    /**
     * Time when the location was updated represented as a String.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestingLocationUpdates = false;
        requestAllPermissions(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
    }

    /**
     * 앱 사용에 필요한 권한을 Array 로 입력 ('Manifest.permission.필요권한')
     */
    @SuppressLint("MissingPermission")
    private static void requestAllPermissions(Context context) {
        String[] permissions = {ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA};
        Permissions.check(context/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                requestingLocationUpdates = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                requestingLocationUpdates = false;
                Toast.makeText(context, "위치정보를 사용할 수 없습니다", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                onLocationUpdate(currentLocation);
            }
        };
    }

    protected abstract void onLocationUpdate(Location location);

    /**
     * Sets up the location request.
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MS);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onResume() {
        if (!locationManager.isProviderEnabled(GPS_PROVIDER)) {
            showMessageDialog(1, getString(R.string.popup_location_on), false);
        }
        if (requestingLocationUpdates || currentLocation == null) startLocationUpdates();
        super.onResume();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient
                .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onPause() {
        // Remove location updates to save battery.
        stopLocationUpdates();
        super.onPause();
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!requestingLocationUpdates) return;
        // Recommended in applications that request frequent location updates.
        fusedLocationClient
                .removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, task -> requestingLocationUpdates = false);
    }
}
