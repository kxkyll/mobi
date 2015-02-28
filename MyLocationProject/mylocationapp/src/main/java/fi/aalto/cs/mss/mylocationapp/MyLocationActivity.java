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
package fi.aalto.cs.mss.mylocationapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import fi.aalto.cs.mss.mylocationcommon.MyLocationCommon;
import fi.aalto.cs.mss.mylocationapp.R;
import fi.aalto.mss.mylocationcommon.IMyLocationListener;
import fi.aalto.mss.mylocationcommon.IMyLocationServiceInterface;

/**
 * Example of binding and unbinding to the remote service. This demonstrates the
 * implementation of a service which the client will bind to, interacting with
 * it through a Messenger interface.</p>
 */
public class MyLocationActivity extends Activity {
    private IMyLocationServiceInterface mService;


    private static final String TAG = "MyLocationApp";

    /** Messenger for communicating with service. */
    //private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mIsBound;

    /** Some text view we are using to show state information. */
    private TextView mCallbackText;

    private Handler handler;

    private String currentLocation = "no location yet";
            //getString(R.string.field_locality_default);

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IMyLocationServiceInterface.Stub.asInterface(service);
            mIsBound = true;
            currentLocation = getString(R.string.field_locality_default);

            try {
                currentLocation = mService.locationRequest();
                mService.registerLocationListener(serviceListener);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mCallbackText.setText(currentLocation);


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
        }
    };

    private IMyLocationListener.Stub serviceListener = new IMyLocationListener.Stub() {

        @Override
        public void handleChangedLocation(String newLocation) throws RemoteException {
            Log.d(TAG, "handleChangedLocation ----------- newLocation: " +newLocation);
            if (newLocation.length()>0) {
                Log.d(TAG, "Updating location--------------------------" );
                //mCallbackText.setText(newLocation);
                currentLocation = newLocation;
                updateView();
            }

        }
    };

    private void updateView(){
        Log.d(TAG, "Updating view--------------------------" );
        handler.post(new Runnable(){
            @Override
            public void run(){
                Log.d(TAG, "Handler at work-------------------------" );
                mCallbackText.setText(currentLocation);
            }

        });
    }

    /**
     * Handler of incoming messages from service.
     */
    /*class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyLocationCommon.MSG_UPDATE_LOCATION:
                    //Address address = msg.getData().getParcelable(
                    //        MyLocationCommon.KEY_LOCATION_UPDATE_PARCELABLE);
                    String location = msg.getData().getString(MyLocationCommon.KEY_LOCATION_UPDATE_STRING);
                    if (location.length()>0) {
                    //if (address != null) {
                    //    String location = formatAddress(address);
                        Log.d(TAG, "Received location update: '" + location + "'");
                        mCallbackText.setText(location);
                    } else {
                        mCallbackText.setText(getString(R.string.field_locality_default));
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
*/
    /**
     * Target published for clients to send messages to IncomingHandler.
     */
   // private final Messenger mMessenger = new Messenger(new IncomingHandler());


    /**
     * Class for interacting with the main interface of the service.
     */
    /*private final ServiceConnection mConnection = new ServiceConnection() {
        /*
         * This is called when the connection with the service has been
         * established, giving us the service object we can use to interact with
         * the service. We are communicating with our service through an IDL
         * interface, so get a client-side representation of that from the raw
         * service object.
         */
      /*  @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d(TAG, "Connected to location service");

            // We want to monitor the service for as long as we are connected to
            // it
            try {
                Message msg = Message.obtain(null, MyLocationCommon.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                Log.d(TAG, "Registered with location service");

                // Trigger location update
                msg = Message.obtain(null, MyLocationCommon.MSG_UPDATE_LOCATION);
                mService.send(msg);
                Log.d(TAG, "Triggered location update");
            } catch (RemoteException e) {
                /*
                 * In this case the service has crashed before we could even do
                 * anything with it; we can count on soon being disconnected
                 * (and then reconnected if it can be restarted) so there is no
                 * need to do anything here.
                 */
/*            }
        }
*/
        /*
         * This is called when the connection with the service has been
         * unexpectedly disconnected -- that is, its process crashed.
         */
/*        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);
        handler = new Handler();

        mCallbackText = (TextView) findViewById(R.id.field_locality);
    }

    /*
     * We only need to interact with the service while our activity is visible,
     * so we bind during onStart()...
     */
    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
        Log.d(TAG, "Binding with location service");
    }

    /*
     * ...and unbind during onStop().
     */
    @Override
    protected void onStop() {
        super.onStop();
        try {
            mService.unregisterLocationListener(serviceListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        doUnbindService();
        Log.d(TAG, "Unbinding from location service");
    }

    /**
     * Establish a connection with the service.
     */
    void doBindService() {
        /*
         * We use an explicit class name because there is no reason to be able
         * to let other applications replace our component.
         */
        Intent intent = new Intent();
        intent.setClassName("fi.aalto.cs.mss.mylocationservice",
                "fi.aalto.cs.mss.mylocationservice.MyLocationService");

        try {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            /*
             * Catch generic Exception to prevent application from crashing with
             * no indication of the reason to the user. The most common case
             * here is that MyLocationApp has been deployed before
             * MyLocationService. Hence we do not have the required permission
             * to bind to the service and the above bindService call throws a
             * SecurityException.
             *
             * In almost all cases it is inappropriate to catch generic
             * Exception or Throwable. It means that Exceptions never expected
             * (including RuntimeExceptions like ClassCastException) end up
             * getting caught in application-level error handling, obscuring the
             * failure handling properties of the application code. In most
             * cases different types of exception should not be handled in the
             * same way, anyway.
             *
             * There are rare exceptions to this rule: certain test code and
             * top-level code where where it is appropriate to catch all kinds
             * of errors (to prevent them from showing up in a UI, or to keep a
             * batch job running). Think very carefully before doing this,
             * though, and explain in comments why it is safe in this place.
             */

            // Tell the user we failed to bind to the service and exit
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }

        mIsBound = true;
    }

    /**
     * Free the connection with the service.
     */
    void doUnbindService() {
        if (mIsBound) {
            /*
             * If we have received the service, and hence registered with it,
             * then now is the time to unregister.
             */
            /*if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MyLocationCommon.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                    Log.d(TAG, "Unregistered with location service");
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has
                    // crashed.
                }
            }
            */
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }



    /*
     * Format the address lines (if available), thoroughfare, sub-administrative
     * area and country name.
     */
    private String formatAddress(Address address) {
        if (address == null) {
            return null;
        }

        StringBuilder addressText = new StringBuilder();

        // If there's a street address, use it...
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressText.append(address.getAddressLine(i));
            addressText.append(' ');
        }

        // ..otherwise fall back to using any fields which might be available
        if (address.getMaxAddressLineIndex() < 0) {
            // The thoroughfare is usually the street name
            if (address.getThoroughfare() != null) {
                addressText.append(address.getThoroughfare());
                addressText.append(' ');
            }

            // The sub-administrative area is usually a city
            if (address.getSubAdminArea() != null) {
                addressText.append(address.getSubAdminArea());
                addressText.append(' ');
            }

            // The country of the address
            if (address.getCountryName() != null)
                addressText.append(address.getCountryName());
        }

        // Return the text
        return addressText.toString();
    }
}
