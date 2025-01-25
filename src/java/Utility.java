
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading and saving students to and from a file.
 */
public class Utility {

    /**
     * Loads students from a specified file.
     *
     * @param fileName the name of the file to load students from
     * @return a list of students loaded from the file
     */
    public static List<String> loadChatsFromLogFile(String fileName) {
        List<String> messages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                messages.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading chats from file: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Saves students to a specified file.
     *
     * @param clients the list of students to save
     * @param fileName the name of the file to save students to
     */
    public static void saveChatsToLogFile(List<ClientHandler> clients, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (ClientHandler client : clients) {
                for (String message : client.getClientMessages()) {
                    writer.write(client.getClientName() + ": " + message + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving chats to file: " + e.getMessage());
        }
    }
}