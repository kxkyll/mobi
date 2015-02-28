// IMyLocationListener.aidl
package fi.aalto.mss.mylocationcommon;


// Declare any non-default types here with import statements

interface IMyLocationListener {
    void handleChangedLocation(String newLocation);

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //        double aDouble, String aString);
}
