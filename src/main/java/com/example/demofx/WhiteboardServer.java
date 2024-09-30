package com.example.demofx;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class WhiteboardServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clients = new CopyOnWriteArraySet<>();
    private static Set<String> validPins = new HashSet<>();  // Store valid PINs

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running...");
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Example of adding valid PINs; you can customize this
        validPins.add("1234");  // Add valid PINs here
        validPins.add("5678");

        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String pin;  // Store the pin for the client

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.add(out);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input;

                // First read the PIN
                if ((input = in.readLine()) != null) {
                    pin = input;  // Store the received PIN

                    // Check if the PIN is valid
                    if (!validPins.contains(pin)) {
                        System.out.println("Invalid PIN: " + pin);
                        out.println("Invalid PIN! You cannot join.");
                        return;  // Exit if the PIN is invalid
                    }
                }

                // Handle drawing data
                while ((input = in.readLine()) != null) {
                    System.out.println("Received: " + input);
                    for (PrintWriter client : clients) {
                        client.println(input);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                clients.remove(out);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
