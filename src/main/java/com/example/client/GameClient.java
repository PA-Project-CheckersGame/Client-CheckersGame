package com.example.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameClient {

    private static Socket socket = null;
    private static BufferedReader in = null;
    private static BufferedWriter out = null;

    public GameClient() {}

    public GameClient(String address, int port) {
        try {
            // Initiaza conexiunea cu serverul
            socket = new Socket(address, port);
            System.out.println("Conectat la server!");

            // Initiaza streamurile de intrare/iesire
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        }
        catch(IOException i) {
            System.out.println("Eroare la crearea conexiunii cu serverul: " + i.getMessage());
        }
    }

    public static void sendRequestToServer(String request){
        try{
            out.write(request);
            out.newLine();
            out.flush();
        }
        catch (IOException e){
            System.out.println("Eroare la trimiterea mesajului catre server: " + e.getMessage());
            close();
        }
    }

    public static void reciveResponseFromServer(){

    }


    public static void close() {
        // Inchide conexiunea cu serverul
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        }
        catch(IOException i) {
            System.out.println("Eroare: " + i.getMessage());
        }
    }
}
