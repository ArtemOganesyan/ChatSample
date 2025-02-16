import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost"; // Server address
    private static final int SERVER_PORT = 10100; // Server port

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("|===============================|\n" +
                               "|      Welcome to our Chat!     |\n" +
                               "|===============================|");
            System.out.println("Enter your name: ");
            String clientName = stdIn.readLine();
            out.println(clientName);

            System.out.println("Connected to chat server as " + clientName);

            // Thread to read messages from the server
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (Exception e) {
                    System.out.println("Server closed the connection: " + e.getMessage());
                }
            }).start();

            // Read user input and send messages to the server
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }
        } catch (Exception e) {
            System.out.println("Server not connected : " + e.getMessage());
        }
    }
}