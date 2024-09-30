package com.example.demofx;

import javafx.application.Platform;

import java.io.*;
import java.net.*;

public class WhiteboardClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public WhiteboardClient(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, 12345);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        listenForDrawingData();  // Start listening for incoming drawing data
    }

    public void sendDrawingData(String data) {
        out.println(data);  // Send data to server
    }

    private void listenForDrawingData() {
        new Thread(() -> {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    // Make a final copy of the input variable
                    String finalInput = input;  // Create a final copy

                    // Notify the Main class about new drawing data
                    Platform.runLater(() -> Main.receiveDrawingData(finalInput));  // Use the final copy
                }
            } catch (IOException e) {
                System.out.println("Error receiving data: " + e.getMessage());
            }
        }).start();
    }
}
