package com.portnov.ChatServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler extends Thread {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private Socket clientSocket;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        clients.add(this);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the client's name
            clientName = in.readLine();
            System.out.println(clientName + " has joined the chat.");
            broadcastMessage(clientName + " has joined the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("\\")) {
                    handleCommand(message.substring(1));
                } else {
                    System.out.println(clientName + ": " + message);
                    broadcastMessage(clientName + ": " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            clients.remove(this);
            System.out.println(clientName + " has left the chat.");
            broadcastMessage(clientName + " has left the chat.");
        }
    }

    private void handleCommand(String command) {
        switch (command.toLowerCase()) {
            case "ping":
                out.println("PONG");
                break;
            case "time":
                out.println("Server time: " + System.currentTimeMillis());
                break;
            case "list":
                out.println("Connected clients: " + getClientNames());
                break;
            default:
                out.println("Unknown command: " + command);
                break;
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            if (client != this) {
                client.out.println(message);
            }
        }
    }

    private String getClientNames() {
        StringBuilder clientNames = new StringBuilder();
        for (ClientHandler client : clients) {
            if (clientNames.length() > 0) {
                clientNames.append(", ");
            }
            clientNames.append(client.clientName);
        }
        return clientNames.toString();
    }
}