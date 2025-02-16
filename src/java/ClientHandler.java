import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler extends Thread {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private Socket clientSocket;
    private PrintWriter out;
    private String clientName;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private List<String> clientMessages = new CopyOnWriteArrayList<>();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        clients.add(this);
    }

    public String getClientName() {
        return clientName;
    }

    public List<String> getClientMessages() {
        return clientMessages;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the client's name
            clientName = in.readLine();
            String joinMessage = getCurrentTimestamp() + " " + clientName + " has joined the chat.";
            System.out.println(joinMessage);
            broadcastMessage(joinMessage);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("\\")) {
                    handleCommand(message.substring(1));
                } else {
                    String formattedMessage = clientName + ": " + message;
                    System.out.println(formattedMessage);
                    if (!clientMessages.contains(message)) {
                        clientMessages.add(message);
                        Utility.saveChatsToLogFile(clients, "chatLog.JSON");
                    }
                    broadcastMessage(formattedMessage);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
            String joinMessage = getCurrentTimestamp() + " " + clientName + " has left the chat.";
            System.out.println(joinMessage);
            broadcastMessage(joinMessage);
        }
    }

    private void handleCommand(String command) {
        if (command.startsWith("message ")) {
            String[] parts = command.split(" ", 3);
            String receiverName = parts[1];
            String privateMessage = parts[2];
            sendPrivateMessage(receiverName, privateMessage);
        } else {
            switch (command.toLowerCase()) {
                case "ping":
                    out.println("PONG");
                    break;
                case "time":
                    out.println("Server time: " + getServerTime());
                    break;
                case "list":
                    out.println("Connected clients: " + getClientNames());
                    break;
                case "log":
                    out.println("Chat log:");
                    for (ClientHandler client : clients) {
                        for (String message : client.getClientMessages()) {
                            out.println(getCurrentTimestamp() + "| " + client.getClientName() + ": " + message);
                        }
                    }
                    break;
                case "exit":
                    out.println("Exiting chat...");
                    String leaveMessage = getCurrentTimestamp() + " " + clientName + " has left the chat.";
                    System.out.println(leaveMessage);
                    broadcastMessage(leaveMessage);
                    try {
                        clients.remove(this);
                        clientSocket.close();
                        System.exit(0);

                    } catch (Exception e) {
                        System.out.println("Error closing client socket: " + e.getMessage());
                    }
                    break;
                default:
                    out.println("Unknown command: " + command);
                    break;
            }
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
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
    private void sendPrivateMessage(String receiverName, String message) {
        for (ClientHandler client : clients) {
            if (client.clientName.equalsIgnoreCase(receiverName)) {
                client.out.println("Private message from " + clientName + ": " + message);
                out.println("Private message to " + receiverName + ": " + message);
                return;
            }
        }
        out.println("User " + receiverName + " not found.");
    }
    private String getServerTime() {
        return LocalDateTime.now().format(formatter);
    }
}