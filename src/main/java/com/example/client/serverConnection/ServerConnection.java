package com.example.client.serverConnection;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerConnection {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private List<ServerResponseListener> listeners = new ArrayList<>();

    public ServerConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        new Thread(this::listenForResponses).start();
    }

    public void addListener(ServerResponseListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ServerResponseListener listener) {
        listeners.remove(listener);
    }

    public void sendRequest(String request) {
        writer.println(request);
    }

    private void listenForResponses() {
        try {
            while (true) {
                String response = reader.readLine();
                for (ServerResponseListener listener : listeners) {
                    Platform.runLater(() -> listener.onServerResponse(response));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}