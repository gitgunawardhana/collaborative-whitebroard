package com.example.demofx;

import javafx.application.Platform;

import java.io.*;
import java.net.*;

public class WhiteboardClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public WhiteboardClient(String serverAddress, String pin) throws IOException {
        socket = new Socket(serverAddress, 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendPin(pin);  // Send the PIN to the server
        listenForDrawingData();  // Start listening for incoming drawing data
    }

    private void sendPin(String pin) {
        out.println(pin);  // Send the PIN to the server
    }

    public void sendDrawingData(String data) {
        out.println(data);  // Send data to server
    }

    private void listenForDrawingData() {
        new Thread(() -> {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    String finalInput = input;  // Create a final copy
                    Platform.runLater(() -> Main.receiveDrawingData(finalInput));  // Use the final copy
                }
            } catch (IOException e) {
                System.out.println("Error receiving data: " + e.getMessage());
            }
        }).start();
    }
}
