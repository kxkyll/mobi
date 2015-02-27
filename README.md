Programming assignment 2
========================

In order to provide the separation of privileges described in the assignment
description, the service and client are separated into two distinct
applications; `MyLocationService` and `MyLocationApp`.

Communication between the two occurs via two `Messenger` instances. The client
receives a handle to the service `Messenger` `Binder` interface when binding to
the service. The service is started automatically by the Android system when the
first client binds to it. Binding to the service is restricted via a dangerous
level `fi.aalto.mss.permission.ACCESS_AGGREGATED_LOCATION` permission.

The message interface between the service and client is defined in a separate
library module `mylocationcommon`, referenced by both the service and client
apps.

When the client registers to receive location updates, it passes a reference to
a `Messenger` instance of its own the service can subsequently use to reply to
the client. The service maintains a list of reply handles to registered clients.
Concurrent requests are placed in a work queue and processed one-by-one. The
service performs reverse geocoding of the location coordinates and passes the
resolved address to client as a `Parcelable` attached to the replied message.
Changes in location are passed to each registered client as the service receives
new coordinates. Clients may unregister at any time by sending a message to
indicate this to the service. Once all clients have unregistered, the service
automatically ceases execution. 

Deployment notes
----------------

In order to ensure that the permission required to bind to the service is
defined at the time the `MyLocationApp` is installed, make sure the deploy the
`MyLocationService` to the device before `MyLocationApp`.

