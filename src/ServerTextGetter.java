import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerTextGetter extends Thread {

    BufferedReader reader;
    JTextArea textArea;

    public ServerTextGetter(JTextArea ta, BufferedReader bf) {
        this.reader = bf;
        textArea = ta;
    }


    @Override
    public void run() {
        try {
            while (true) {
                String message = reader.readLine();
                textArea.append(message + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}