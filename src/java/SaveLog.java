import java.io.FileWriter;
import java.io.IOException;

public class SaveLog{
    private static final String SAVE_FILE = "chat.log";
    private static final String SAVE_SUCCESS = "Chat log saved to " + SAVE_FILE;
    private static final String SAVE_ERROR = "Error saving chat log";

    public static void saveChatLog(String log) {
        try (FileWriter writer = new FileWriter(SAVE_FILE, true)) {
            writer.write(log + System.lineSeparator());
            System.out.println(SAVE_SUCCESS);
        } catch (IOException e) {
            System.out.println(SAVE_ERROR);
            e.printStackTrace();
        }
    }
}
