package com.example.demofx;

import java.util.HashSet;
import java.util.Set;

public class WhiteboardSession {
    private String pin;
    private Set<WhiteboardClient> clients;

    public WhiteboardSession(String pin) {
        this.pin = pin;
        this.clients = new HashSet<>();
    }

    public String getPin() {
        return pin;
    }

    public void addClient(WhiteboardClient client) {
        clients.add(client);
    }

    public void removeClient(WhiteboardClient client) {
        clients.remove(client);
        if (clients.isEmpty()) {
            // Handle cleanup if needed
        }
    }

    public Set<WhiteboardClient> getClients() {
        return clients;
    }
}
