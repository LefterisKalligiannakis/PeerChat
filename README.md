# PeerChat

**PeerChat** is an Android application developed as part of the Mobile Computer Networks course at the University of Cyprus. It enables real-time, peer-to-peer messaging between nearby devices using Android's Wi-Fi P2P (Wi-Fi Direct) API.

## Features

- **Peer Discovery**: Locate nearby devices using Wi-Fi Direct.
- **Real-Time Messaging**: Exchange messages instantly with connected peers.
- **Chat Logs**: Access and review past conversations.
- **Location Sharing**: View peer location on Google Maps.

## Application Structure

### Activities

**MainActivity.java**
  - Initiates peer discovery and manages connections.
  - Provides access to chat logs.

<img src="https://github.com/user-attachments/assets/db07abfb-e920-4a87-8ed2-9a8ab6a08d09" height=20% width=20%>

##

**MessageActivity.java**
- Facilitates real-time message exchange between peers.
- Updates location information.

<img src="https://github.com/user-attachments/assets/e894bc38-fadd-4f38-9c7a-3417424332e9" height=20% width=20%>

##
 
**ChatLogActivity.java**
- Displays previously stored messages from past conversations.

<img src="https://github.com/user-attachments/assets/3854743e-a72a-419c-b822-6aaf1a4848fb" height=20% width=20%>

##

**MapsActivity.java**
- Displays peer's location on Google Maps using the Maps API.

<img src="https://github.com/user-attachments/assets/6e351e3d-52b5-454e-adb4-3ceed8ed831e" height=20% width=20%>

##

### Core Classes

**ConnectionManager.java**
- Handles setup and teardown of Wi-Fi P2P connections.

##

**WIFIBroadcastReceiver.java**
- Monitors Wi-Fi Direct-related broadcasts and updates the peer list.

##

**MessageServer.java**  
  -  Listens for incoming socket connections in a background thread. Once a peer connects, it receives messages, logs them to the local database via `ChatDBHelper`, and relays them to the UI through a `MessageListener` interface.

##

**MessageClient.java**  
  -  Handles outbound communication to a connected peer's `MessageServer`. 
  Establishes a socket connection, sends messages through the output stream, 
  and logs sent messages in the local database.

##

**ChatDBHelper.java**  
-  Handles local SQLite database operations, including:
    - Creating and managing the messages table.
    - Inserting incoming and outgoing messages.
    - Fetching past conversations for display in the ChatLogActivity.

## Usage

1. **Launch PeerChat** on two devices.
2. **Discover Peers** using the Main Activity.
3. **Establish Connection** by selecting a device.
4. **Send Messages** through the Message Activity.
5. **View location information** through the Maps Activity accessed through the top right button in the message activity.
6. **View Previous Conversations** using the Chat Log screen.

The source code is located under: `app/src/main/java/com/example/peerchat`

The app requires an API key for google maps that can be created through the Google Cloud Platform and must replace "API_KEY" in the AndroidManifest.xml
