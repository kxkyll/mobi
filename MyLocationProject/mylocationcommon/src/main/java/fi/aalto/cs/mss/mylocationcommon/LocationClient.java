package fi.aalto.cs.mss.mylocationcommon;

import fi.aalto.mss.mylocationcommon.IMyLocationListener;

/**
 * Created by kxkyllon on 28.2.2015.
 */
public class LocationClient {
    public IMyLocationListener listener;
    public int listenerUid;

    public LocationClient(IMyLocationListener mlistener, int callingUid) {
        this.listener = mlistener;
        this.listenerUid = callingUid;
    }
}
