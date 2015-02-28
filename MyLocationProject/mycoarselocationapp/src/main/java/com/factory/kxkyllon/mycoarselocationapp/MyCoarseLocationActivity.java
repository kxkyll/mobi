package com.factory.kxkyllon.mycoarselocationapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import fi.aalto.cs.mss.mylocationcommon.MyLocationCommon;
import com.factory.kxkyllon.mycoarselocationapp.R;
import fi.aalto.mss.mylocationcommon.IMyLocationListener;
import fi.aalto.mss.mylocationcommon.IMyLocationServiceInterface;


public class MyCoarseLocationActivity extends Activity {

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
            currentLocation = getString(R.string.location_default);

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
            Log.d(TAG, "handleChangedLocation ----------- newLocation: " + newLocation);
            if (newLocation.length()>0) {
                Log.d(TAG, "Updating location--------------------------" );
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coarse_location);

        handler = new Handler();
        mCallbackText = (TextView) findViewById(R.id.field_location);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_coarse_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }



}
