# ChatSocket

This is a simple chat demo for socket.io and Android. You can connect to [https://socket-io-chat.now.sh](http://socket.io/blog/native-socket-io-and-android/) using this app. 

# <a href="https://www.behance.net/mrh610371211" rel="nofollow">
  <img align="left" alt="Noor Yasser | Youtube " width="21px" src="https://user-images.githubusercontent.com/41232970/102919173-0e8cfe80-4491-11eb-9706-cdebd4f610ff.png" style="max-width:300%; max-height:150%;"> Socket Android Youtube Tutorial </a>

# ✨ Features Project Android:
- 100% Kotlin
- MVVM architecture 
- Android architecture components
- Navigation Jetpack 
- Room Database
- Single activity
- dataBinding 
- Coroutines 

# ✨ Features Project Server: 
- Node js 
- Express

<br/> 

![Group 3121](https://user-images.githubusercontent.com/41232970/102915555-b0f5b380-448a-11eb-88f9-8039d41c03ad.png)

<br/> 

Installing the Dependencies
The first step is to install the Java Socket.IO client with Gradle.

For this app, we just add the dependency to build.gradle:

```groovy
// app/build.gradle
dependencies {
    ...
    implementation 'com.github.nkzawa:socket.io-client:0.6.0'
}
```
We must remember adding the internet permission to AndroidManifest.xml.
```groovy
// app/build.gradle
<!-- app/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    ...
</manifest>
```

## Using socket in Activity and Fragment

First, we have to initialize a new instance of Socket.IO as follows:
```groovy
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

private Socket mSocket;
{
    try {
        mSocket = IO.socket("http://chat.socket.io");
    } catch (URISyntaxException e) {}
}

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSocket.connect();
}
```

IO.socket() returns a socket for http://chat.socket.io with the default options. Notice that the method caches the result, so you can always get a same Socket instance for an url from any Activity or Fragment.
And we explicitly call connect() to establish the connection here (unlike the JavaScript client). In this app, we use onCreate lifecycle callback for that, but it actually depends on your application.

## Emitting events

Sending data looks as follows. In this case, we send a string but you can do JSON data too with the org.json package, and even binary data is supported as well!

```groovy
private EditText mInputMessageView;

private void attemptSend() {
    String message = mInputMessageView.getText().toString().trim();
    if (TextUtils.isEmpty(message)) {
        return;
    }

    mInputMessageView.setText("");
    mSocket.emit("new message", message);
}
```

## Listening on events

ike I mentioned earlier, Socket.IO is bidirectional, which means we can send events to the server, but also at any time during the communication the server can send events to us.

We then can make the socket listen an event on onCreate lifecycle callback.

```groovy
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mSocket.on("new message", onNewMessage);
    mSocket.connect();
}
```
With this we listen on the new message event to receive messages from other users.

```groovy

import com.github.nkzawa.emitter.Emitter;

private Emitter.Listener onNewMessage = new Emitter.Listener() {
    @Override
    public void call(final Object... args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException e) {
                    return;
                }

                // add the message to view
                addMessage(username, message);
            }
        });
    }
};

```
This is what onNewMessage looks like. A listener is an instance of Emitter.Listener and must be implemented the call method. Youll notice that inside of call() is wrapped by Activity#runOnUiThread(), that is because the callback is always called on another thread from Android UI thread, thus we have to make sure that adding a message to view happens on the UI thread.

## Managing Socket State

Since an Android Activity has its own lifecycle, we should carefully manage the state of the socket also to avoid problems like memory leaks. In this app, we’ll close the socket connection and remove all listeners on onDestroy callback of Activity.

```groovy

@Override
public void onDestroy() {
    super.onDestroy();

    mSocket.disconnect();
    mSocket.off("new message", onNewMessage);
}

```
