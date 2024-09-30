package com.example.demofx;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class WhiteboardServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clients = new CopyOnWriteArraySet<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running...");
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.add(out);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input;

                while ((input = in.readLine()) != null) {
                    System.out.println("Received: " + input);  // Log the received data
                    for (PrintWriter client : clients) {
                        client.println(input);
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                clients.remove(out);  // Remove the client on disconnection
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}