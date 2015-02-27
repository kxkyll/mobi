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
package fi.aalto.cs.mss.mylocationcommon;

public final class MyLocationCommon {

    /**
     * Command to the service to register a client, receiving callbacks from the
     * service. The Message's replyTo field must be a Messenger of the client
     * where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, to stop receiving
     * callbacks from the service. The Message's replyTo field must be a
     * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to send location update. This can be sent to the
     * service to trigger a location update, and will be sent by the service to
     * any registered clients with the new location.
     */
    public static final int MSG_UPDATE_LOCATION = 3;

    /**
     * Key used to identify the parcelable location update data in Bundle passed
     * from service to client.
     */
    public static final String KEY_LOCATION_UPDATE_PARCELABLE = "address";

    /**
     * Key used to identify the location update data in string format in Bundle passed
     * from service to client.
     */

    public static final String KEY_LOCATION_UPDATE_STRING = "addressString";

}