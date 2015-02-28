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
 */
package fi.aalto.cs.mss.mylocationservice;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.LocationServices;

import fi.aalto.cs.mss.mylocationcommon.LocationClient;
import fi.aalto.cs.mss.mylocationcommon.MyLocationCommon;
import fi.aalto.cs.mss.mylocationservice.MyAbstractLocationService;
import fi.aalto.mss.mylocationcommon.IMyLocationListener;
import fi.aalto.mss.mylocationcommon.IMyLocationServiceInterface;

public class MyLocationService extends MyAbstractLocationService {

    // Tag used for log message
    private static final String TAG = "MyLocationService";

    /** List used to keep track of all current registered clients. */
    //protected final ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    /** Holds the address indicated by the last location change */
    protected Address mAddress;

    private StringBuilder fineGrainAddressText = new StringBuilder();
    private StringBuilder coarseGrainAddressText = new StringBuilder();

    /** Geocoder instance used to perform reverse geocoding */
    private Geocoder geocoder;

    private List<IMyLocationListener> listeners = new ArrayList<IMyLocationListener>();
    private List<LocationClient> locationListeners = new ArrayList<LocationClient>();
    private List<Integer> fineGrainListed = new ArrayList<Integer>();


    private void initializeFineGrainListed() {
        fineGrainListed.add(10066);
    }

    private IMyLocationServiceInterface.Stub mBinder = new IMyLocationServiceInterface.Stub() {

        public String locationRequest() {
            if (mGoogleApiClient.isConnected()) {
                Log.d(TAG, "Google Api Client is connected");
                Location location = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                updateCurrentLocation(location);
            }
            if (isClientFineGradeListed(Binder.getCallingPid())){
                Log.d(TAG, "sending location as response to locationRequest "+fineGrainAddressText.toString());
                return fineGrainAddressText.toString();
            }else {
                Log.d(TAG, "sending location as response to locationRequest "+coarseGrainAddressText.toString());
                return coarseGrainAddressText.toString();
            }

        }
        public void registerLocationListener(IMyLocationListener mlistener){

            int callingUid = Binder.getCallingUid();
            LocationClient client = new LocationClient(mlistener, callingUid);
            locationListeners.add(client);
            //listeners.add(mlistener);
            Log.d(TAG, "locationListeners has this many listeners: " +locationListeners.size());
            Log.d(TAG, "Binder.getCallingUid returned: "+callingUid);


        }
        public void unregisterLocationListener(IMyLocationListener mlistener){
            if (mlistener == null){
                return;
            }
            LocationClient removeThis =  null;
            for (LocationClient client: locationListeners){
                if (client.listener.equals(mlistener)) {
                    removeThis = client;
                }
            }
            if (removeThis != null){
                locationListeners.remove(removeThis);
            }

        }

    };


    private boolean isClientFineGradeListed(int listenerUid) {
        return fineGrainListed.contains(listenerUid);

    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
  //  final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        Log.d(TAG, "Service starting");
        super.onCreate();
        fineGrainAddressText.append("Seeking exact location");
        coarseGrainAddressText.append("Seeking city location");
        initializeFineGrainListed();

        // Initialize Geocoder instance used retrieve the the current address
        geocoder = new Geocoder(this, Locale.getDefault());

        // Tell the user we started.
        Toast.makeText(this, getString(R.string.notify_started),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            // After disconnect() is called, the client is considered "dead"
            mGoogleApiClient.disconnect();
        }

        // Tell the user we stopped
        Toast.makeText(this, getString(R.string.notify_stopped),
                Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Service stopping");
        super.onDestroy();
    }

    /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Location client bound to service, returning aidl binder");
        //return mMessenger.getBinder();
        return mBinder;
    }

    /*
     * Called by Location Services when an location update is available
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "-------------------onLocationChanged---------------------- ");
        updateCurrentLocation(location);
        sendLocationUpdate();
    }

    /**
     * Update cached location to specified location.
     *
     * @param location
     *            New location
     */
    private void updateCurrentLocation(Location location ) {
        if (location == null) {
            return;
        }
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && addresses.size() > 0) {
                Log.d(TAG, "-------------------updateCurrentLocation---- new address received--------------------- ");
                mAddress = addresses.get(0);
                formatAddress(mAddress);
            }
        } catch (IOException e) {
            Log.e(TAG, "Reverse geocoding failed");
            e.printStackTrace();
        }
    }


    /**
     * Send location update to clients.
     */

    private void sendLocationUpdate(){
        Log.d(TAG, "-------------------sendLocationUpdate------------------------ ");
        for (LocationClient locationListener: locationListeners){
            Log.d(TAG, "-------------------sending to all listeners--------------------- ");
            try {
                String sfine = fineGrainAddressText.toString();
                String scoarse = coarseGrainAddressText.toString();
                Log.e(TAG, "--------------------locationListener uid:  " + locationListener.listenerUid);
                Log.e(TAG, "------fineGrainlist contains:--------------" + fineGrainListed.get(0));
                if (isClientFineGradeListed(locationListener.listenerUid)) {
                    Log.e(TAG, "--------------------sending fine grained location to client " + sfine);

                    locationListener.listener.handleChangedLocation(sfine);

                }else {
                    Log.e(TAG, "--------------------sending coarse grained location to client " + scoarse);
                    locationListener.listener.handleChangedLocation(scoarse);

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    /*private void sendLocationUpdate(){
        Log.d(TAG, "-------------------sendLocationUpdate------------------------ ");
        for (IMyLocationListener listener: listeners){
            Log.d(TAG, "-------------------sending to all listeners--------------------- ");
            try {
                String sfine = fineGrainAddressText.toString();
                String scoarse = coarseGrainAddressText.toString();
                if (isClientFineGradeListed()) {
                    Log.e(TAG, "--------------------sending fine grained location to client " + sfine);

                    listener.handleChangedLocation(sfine);

                }else {
                    Log.e(TAG, "--------------------sending coarse grained location to client " + scoarse);
                    listener.handleChangedLocation(scoarse);

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
*/
    /*
     * Format the address lines (if available), thoroughfare, sub-administrative
     * area and country name.
     */
    private void formatAddress(Address address) {
        if (address == null) {
            return;
        }


        fineGrainAddressText.setLength(0);
        coarseGrainAddressText.setLength(0);

        // If there's a street address, use it...
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {

            fineGrainAddressText.append(address.getAddressLine(i));
            fineGrainAddressText.append(' ');
        }

        // ..otherwise fall back to using any fields which might be available
        if (address.getMaxAddressLineIndex() < 0) {
            // The thoroughfare is usually the street name
            if (address.getThoroughfare() != null) {
                fineGrainAddressText.append(address.getThoroughfare());
                fineGrainAddressText.append(' ');
            }

            // The sub-administrative area is usually a city
            if (address.getSubAdminArea() != null) {
                fineGrainAddressText.append(address.getSubAdminArea());
                fineGrainAddressText.append(' ');

                coarseGrainAddressText.append(address.getSubAdminArea());
            }

            // The country of the address
            if (address.getCountryName() != null) {
                fineGrainAddressText.append(address.getCountryName());
            }
        }
            // The sub-administrative area is usually a city
        if (address.getSubAdminArea() != null) {
            coarseGrainAddressText.append(address.getSubAdminArea());
        }

    }


}