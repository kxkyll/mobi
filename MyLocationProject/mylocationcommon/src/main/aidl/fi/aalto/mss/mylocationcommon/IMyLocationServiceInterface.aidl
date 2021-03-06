// IMyLocationServiceInterface.aidl
package fi.aalto.mss.mylocationcommon;

import fi.aalto.mss.mylocationcommon.IMyLocationListener;

// Declare any non-default types here with import statements

interface IMyLocationServiceInterface {

    String locationRequest();
    void registerLocationListener (IMyLocationListener mlistener);
    void unregisterLocationListener (IMyLocationListener mlistener);
}
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);


