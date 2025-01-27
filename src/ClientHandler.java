import java.Utility;
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
//            System.out.println(clientName + " has joined the chat.");
//            broadcastMessage(clientName + " has joined the chat.");
            String joinMessage = getCurrentTimestamp() + " " + clientName + " has joined the chat.";
            System.out.println(joinMessage);
            broadcastMessage(joinMessage);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("\\")) {
                    handleCommand(message.substring(1));
                } else {
                    System.out.println(clientName + ": " + message);
                    clientMessages.add(message);
                    Utility.saveChatsToLogFile(clients, "chatLog.txt");
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
//            clients.remove(this);
//            System.out.println(clientName + " has left the chat.");
//            broadcastMessage(clientName + " has left the chat.");
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
                    Utility.loadChatsFromLogFile("chatLog.txt").forEach(out::println);
                    for (String message : clientMessages) {
                        out.println(clientName + ": " + message);
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