# AdaptiveHeartBeat


This is the repository for Adaptive keep-alive (heartbeat) work
. The work is setup as an IntelliJ project under the code folder. It was used for experimentation with an Android application for our research work. The research work is currently submitted to IEEE TON.

/code/src:

There are three packages here:

kaserver: This is a simple echo server so that GCMAdaptiveHeartBeater (refer to https://github.com/srautonu/GCMAdaptiveHeartBeater) can connect to it, send keepalive
messages at varying intervals and examine whether the connection is maintained. You can specify the port number as a command line parameter. (The default is 80).
java kaserver.KAServer <port>

kadesktopclient: This is a desktop client that can be used to connect to the test server for KA testing. Command line to use:
java kadesktopclient.KADesktopClient <server> <port>

notificationserver: This is the custom notification server we have implemented. To run it use the following command line:
java notificationserver.NotificationServer <port>

The model based notification generator is in a separate IntelliJ project under code/NotificationGenerator/. To run it, use the following command line:
java NotificationGenerator <DeviceName> <Duration_hours> <RNG_Seed> <NotificationServerName>

Under the same project, there is also a tool (JavaApplication1) for sending a single notification. To run it use the following command line:
java JavaApplication1 <DeviceName>

The notification generators send notification via our custom notification server. However, they have all the code ready for sending notifications via GCM instead. With very minor modifications, they can be pointed to GCM notification service.