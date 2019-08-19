/*
 * This activity tracks device location in order to display points of interest near the user's
 * current location.  Discovered "caches" stay visible and display name and address.
 *
 * Plan for future: show nearby points of interest based on google's Places SDK.  Integrate with
 * Wikipedia API to give information about cache points?
 *
 * Instructions for if it doesn't seem to be working on start up:
 *      Check versions of google APIs in the .gradle files under "dependencies".  Fiddling with these sometimes helps
 *
 * Adjusting device location in the emulator:
 *      While the device emulator is active, click the "..." at the bottom of the control panel to open the
 *      Extended Controls. You can adjust the Latitude and Longitude here and update it in the emulator by clicking send.
 */

package edu.stanford.googlemapsptest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation;
    private PlaceDetectionClient mPlaceDetectionClient;

    private int DEFAULT_ZOOM;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Create list of caches; cache1 is Hoover Tower--the logic of construction should be cleaned up.
    private double cache1Lat = 37.427611;
    private double cache1Lng = -122.166971;
    private double cache2Lat = 37.4220034;
    private double cache2Lng = -122.084031;

    private Location cache1Location = new Location("Cache1: Hoover Tower");
    private Location cache2Location = new Location("Cache2: Googleplex Default");
    private List<Location> caches = new ArrayList<>();

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private static double currentLat = 0;
    private static double currentLon = 0;

    private static final String TAG = "LocationFind";

    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // This is a messy way to construct the locations of points of interest
        // it should be put in a separate function
        cache1Location.setLatitude(cache1Lat);
        cache1Location.setLongitude(cache1Lng);
        cache2Location.setLatitude(cache2Lat);
        cache2Location.setLongitude(cache2Lng);

        caches.add(cache1Location);
        caches.add(cache2Location);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Probably too far out, adjust based on size of area
        DEFAULT_ZOOM = 11;

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationUI();
                    checkCacheApproach(location);
                }
            };
        };
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        addListenerLocation();

    }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Call once user gives or revokes location permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            mMap.setMyLocationEnabled(true);
                            checkCacheApproach(mLastKnownLocation);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /*
     * Checks if the user is within 50 ft (currently a test distance) of a point of interest
     */
    private void checkCacheApproach(Location currentUserLocation) {
       Log.d(TAG, "Checking cache approach.  User location is " + currentUserLocation.getLatitude() + ", " + currentUserLocation.getLongitude());
        for(int i=0; i < caches.size(); i++){
            Location cachePosition = caches.get(i);
            float meterDistance = cachePosition.distanceTo(currentUserLocation);
            // 500 is a magic number used for testing.  This is supposed to be a 50ft difference
            if (meterDistance < 500) {
                showCurrentPlace(cachePosition);
            }
        }
    }

    // Adds a marker to the point of interest you are within 50 ft (test distance) of.
    private void showCurrentPlace(@NonNull Location cacheLocation) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        double cacheLat = cacheLocation.getLatitude();
        double cacheLng = cacheLocation.getLongitude();
        LatLng cacheLatLng = new LatLng(cacheLat, cacheLng);

        try {
            addresses = geocoder.getFromLocation(cacheLat, cacheLng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null){
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            //String knownName = addresses.get(0).getFeatureName();

            mMap.addMarker(new MarkerOptions().position(cacheLatLng).title(cacheLocation.getProvider() + " " + address));
        }
    }

    @SuppressLint("MissingPermission")
    private void addListenerLocation() {
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                Location userLocation = new Location("User Location");
                userLocation.setLatitude(currentLat);
                userLocation.setLongitude(currentLon);
                checkCacheApproach(userLocation);

                Toast.makeText(getBaseContext(),currentLat+"-"+currentLon, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                @SuppressLint("MissingPermission") Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(lastKnownLocation!=null){
                    currentLat = lastKnownLocation.getLatitude();
                    currentLon = lastKnownLocation.getLongitude();
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);
    }

}

