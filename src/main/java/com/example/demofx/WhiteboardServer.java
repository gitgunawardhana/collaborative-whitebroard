package com.example.demofx;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class WhiteboardServer {
    private static final int PORT = 12345;
    private static Map<String, Set<PrintWriter>> groups = new HashMap<>();  // Map of PIN to a set of clients
    private static Set<String> validPins = new HashSet<>();  // Store valid PINs

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running...");
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Example of adding valid PINs
        validPins.add("1234");  // First group
        validPins.add("5678");  // Second group
        validPins.add("1278");  // third group

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

                    // Add the client to the correct group based on the PIN
                    groups.computeIfAbsent(pin, k -> new CopyOnWriteArraySet<>()).add(out);
                }

                // Handle drawing data
                while ((input = in.readLine()) != null) {
                    System.out.println("Received: " + input);
                    // Broadcast the message only to clients in the same group (PIN)
                    for (PrintWriter client : groups.get(pin)) {
                        client.println(input);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                if (groups.containsKey(pin)) {
                    groups.get(pin).remove(out);
                    if (groups.get(pin).isEmpty()) {
                        groups.remove(pin);  // Clean up the group if no clients are left
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
