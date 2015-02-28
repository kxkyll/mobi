Mobile Systems Security
Kati Kyllönen
Programming Assignment 3

Done on top of Programming Assignment 2 model answer.

Contains:

mylocationservice - service that provides fine or coarse location data 
mylocationcommons - commons as in Assignment 2 - added aidl files
mylocationapp - client that connects to service via aidl
mycoarselocationapp- client that connects to service via aidl

Description:

The service has a list of application uid's that can have fine grain location info
this is hardcoded at the moment

The application uid can be clarified after it has been installed 
eg. in emulator case one can check it from the visual studio terminal window 
with command:
adb shell dumpsys package fi.aalto.cs.mss.mylocationapp | grep userId=
userId=10066 gids=[]

In my case the userid of mylocationapp was: 10066 and this is currently initialized when service is created

The other client mycoarselocationapp get's only the city level location info

The solution is a bit coarse as it has hardcoded values, but it fullfills 
all requirements.

When client registeres the service checks the uid of the caller and saves it 
to list. Every time the location info is updated the service checks if the 
client can get fine or coarse level location data.

When both clients are installed, the one that is active is served by the 
service.
 


