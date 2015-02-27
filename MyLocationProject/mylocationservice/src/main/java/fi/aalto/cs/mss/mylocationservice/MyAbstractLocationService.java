/*
 * Copyright (C) 2013 University of Helsinki
 * Copyright (C) 2015 Aalto University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions of this file are modifications based on work created and shared by
 * the Android Open Source Project and used according to terms described in the
 * Creative Commons 2.5 Attribution License.
 */
package fi.aalto.cs.mss.mylocationservice;

        import android.app.Service;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GooglePlayServicesClient;
        import com.google.android.gms.common.GooglePlayServicesUtil;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationListener;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;

public abstract class MyAbstractLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Tag used for log message
    private static final String TAG = "MyLocationService";

    /*
     * Frequency parameters for location information
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;

    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
            * FASTEST_INTERVAL_IN_SECONDS;

    /** Holds accuracy and frequency parameters */
    protected LocationRequest mLocationRequest;

    /** Client for location updates */
    protected GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {

        // Bail out if Google Play services is not available
        if (!servicesConnected()) {
            stopSelf();
        }

        /*
         * Request real-time location with updates every five seconds and at
         * most every second. We request high-accuracy location in order to
         * trigger frequent location updates in the emulator, but in real- world
         * conditions, this would only be needed by mapping applications that
         * need to display the location in real-time. This also has the most
         * severe power impact.
         *
         * Most applications definitely want to receive updates at a specified
         * interval, and can receive them faster when available, but still want
         * a low power impact. In such cases, PRIORITY_BALANCED_POWER_ACCURACY
         * combined with a higher fastest interval (such as 1 minute) and a
         * slower periodic interval (such as 60 minutes) may be appropriate.
         * "Balanced' location updates will only assign power blame for the
         * interval set by setInterval(long), but can still receive locations
         * triggered by other applications at a rate up to the fastest interval.
         * This style of request is appropriate for many location aware
         * applications, including background usage. Care should be taken to
         * throttle the fastest interval in case heavy work, such as using the
         * network, is to be performed after receiving an update.
         *
         * PRIORITY_LOW_POWER is appropriate for "city" level accuracy. City
         * level accuracy is considered to be about 10km accuracy. Using a
         * coarse accuracy often consumes less power.
         */
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Establish a new location client, using the enclosing class to handle
        // callbacks
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();
        mGoogleApiClient.connect();
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, we request the current location and
     * start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        Log.d(TAG, "Google Play services API client has connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                                                                 mLocationRequest, this);
    }

    /*
     * Called by Location Services if the connection to the location client
     * is interrupted. Disable any functionality that depend on Google APIs
     * until onConnected() is called.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Google Play services API client has been suspended");
    }

    /*
     * Called by Location Services if the connection attempt to Location
     * Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play services API client has failed to connect");
    }

    /*
     * Check that Google Play services is available.
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) { // Google Play services is
            // available
            Log.d(TAG, "Google Play services is available.");
            return true;
        } else { // Google Play services was not available for some reason
            String error = GooglePlayServicesUtil.getErrorString(resultCode);
            Log.e(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
