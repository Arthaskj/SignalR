// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketTransport implements Transport {
    private static final String RECORD_SEPARATOR = "\u001e";
    private WebSocketClient _webSocket;
    private OnReceiveCallBack onReceiveCallBack;
    private URI _url;

    public WebSocketTransport(String url) throws URISyntaxException {
        // To Do: Format the  incoming URL for a websocket connection.
        _url = new URI(url);
        _webSocket = createWebSocket();
    }

    @Override
    public void start() throws InterruptedException {
        _webSocket.connectBlocking();
        _webSocket.send(createHandshakeMessage() + RECORD_SEPARATOR);
    }

    @Override
    public void send(String message) {
        _webSocket.send(message);
    }

    @Override
    public void setOnReceive(OnReceiveCallBack callback) {
        this.onReceiveCallBack = callback;
    }

    @Override
    public void onReceive(String message) {
        this.onReceiveCallBack.invoke(message);
    }

    private String createHandshakeMessage() {
        Gson gson = new Gson();
        return gson.toJson(new DefaultJsonProtocolHandShakeMessage());
    }

    @Override
    public void stop() {
        _webSocket.closeConnection(0, "HubConnection Stopped");
    }

    private WebSocketClient createWebSocket() {
        return new WebSocketClient(_url) {
             @Override
             public void onOpen(ServerHandshake handshakedata) {
                 System.out.println("Connected to " + _url);
             }

             @Override
             public void onMessage(String message) {
                 onReceive(message);
             }

             @Override
             public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection Closed");
             }

             @Override
             public void onError(Exception ex) {
                System.out.println("Error: " + ex.getMessage());
             }
         };
    }
}
