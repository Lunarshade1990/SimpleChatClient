import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;

public class SimpleChatClient {

    Socket clientSocket;
    BufferedReader reader;
    PrintWriter writer;

    JFrame chatWindow;
    JPanel textPanel;
    JPanel buttonBox;
    JTextArea clientMessage;
    JTextArea serverMessage;
    JScrollPane scrollClientText;
    JScrollPane scrollServerText;
    JTextField nickNameTextArea;
    JTextField ipAdressTextArea;
    JTextField portTextArea;
    JButton connectButton;
    JButton unconnectButton;

    JLabel nickLabel;
    JLabel ipLabel;
    JLabel portLabel;

    String nick;

    public static void main(String[] args) {
        SimpleChatClient scc = new SimpleChatClient();
        scc.run();

    }

    public void run() {
        chatWindow = new JFrame();
        chatWindow.setSize(400,450);
        chatWindow.setResizable(false);
        chatWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        textPanel = new JPanel();
        buttonBox = new JPanel(new VerticalLayout());
        buttonBox.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        clientMessage = new JTextArea(10,15);
        clientMessage.setLineWrap(true);
        clientMessage.setWrapStyleWord(true);
        clientMessage.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        clientMessage.getActionMap().put("send", sendMessageAction);

        scrollClientText = new JScrollPane(clientMessage);
        scrollClientText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollClientText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel clientTextName = new JLabel("Сообщение клиента");

        serverMessage = new JTextArea(10,15);
        serverMessage.setEditable(false);
        serverMessage.setLineWrap(true);
        serverMessage.setWrapStyleWord(true);

        scrollServerText = new JScrollPane(serverMessage);
        scrollServerText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollServerText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel serverTextName = new JLabel("Сообщение сервера");

        textPanel.add(clientTextName);
        textPanel.add(scrollClientText);
        textPanel.add(serverTextName);
        textPanel.add(scrollServerText);

        chatWindow.add(textPanel, BorderLayout.CENTER);

        connectButton = new JButton("Соединиться");
        connectButton.addActionListener(new ConnectionButtonListener());

        unconnectButton = new JButton("Отсоединиться");
        unconnectButton.addActionListener(new UnconnectActionListener());

        JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new SendButtonListener());


        nickLabel = new JLabel("Введите ник: ");
        nickNameTextArea = new JTextField(10);
        ipLabel = new JLabel("Введите IP: ");
        ipAdressTextArea = new JTextField(10);
        portLabel = new JLabel("Введите порт: ");
        portTextArea = new JTextField(10);


        buttonBox.add(nickLabel);
        buttonBox.add(nickNameTextArea);
        buttonBox.add(ipLabel);
        buttonBox.add(ipAdressTextArea);
        buttonBox.add(portLabel);
        buttonBox.add(portTextArea);
        buttonBox.add(connectButton);
        buttonBox.add(unconnectButton);
        buttonBox.add(sendButton);

        chatWindow.add(buttonBox, BorderLayout.WEST);
        chatWindow.setVisible(true);
    }

    public class ConnectionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            String ip = ipAdressTextArea.getText();
            boolean isIP = false;

            int port;
            if (portTextArea.getText().isEmpty() ) port = 0;
            else if (!portTextArea.getText().matches("\\d{4}")) port = -1;
            else port = Integer.parseInt(portTextArea.getText());
            boolean isPort = false;

            boolean isNick = false;
            if (nickNameTextArea.getText().isEmpty()) nick = null;
            else nick = nickNameTextArea.getText();

            if (!ip.isEmpty()) {
                if (ip.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")) {
                    isIP = true;
                    ipLabel.setText("IP OK");
                }
                else  ipLabel.setText("Айпи некорректен!");}
            else ipLabel.setText("Заполните IP!");

            if (port == 0) {
                portLabel.setText("Заполните Порт!");
            } else if (!(port >= 1000 && port <= 9999)) {
                portLabel.setText("Порт некорректен!");
            } else {isPort = true; portLabel.setText("Порт OK");
            }

            if (nick != null) {
                isNick = true;
                nickLabel.setText("Ник OK");
            } else nickLabel.setText("Введите ник!");

            if (isIP && isPort && isNick) {
                try {


                    clientSocket = new Socket(ip, port);

                    if (!clientSocket.isClosed()) {

                        setConnectingEnabled(false);
                        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "utf-8"));
                        writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "utf-8"), true);
                        ServerTextGetter stg = new ServerTextGetter(serverMessage, reader);
                        stg.start();
                        writer.println(nick + " вошёл в чат");
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    }

    public void sendMessage() {
        writer.println(nick + ": " + clientMessage.getText());
        clientMessage.setText("");
        clientMessage.grabFocus();
    }

    Action sendMessageAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    };

    private class UnconnectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (!clientSocket.isClosed()) {
                    writer.println(nick + " вышел из чата");
                    clientSocket.close();
                    setConnectingEnabled(true);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    private void setConnectingEnabled (boolean b) {

        if (b) {
            nickNameTextArea.setEditable(true);
            nickNameTextArea.setBackground(Color.WHITE);
            ipAdressTextArea.setEditable(true);
            ipAdressTextArea.setBackground(Color.WHITE);
            portTextArea.setEditable(true);
            portTextArea.setBackground(Color.WHITE);
            connectButton.setEnabled(true);
        } else {
            ipAdressTextArea.setEditable(false);
            ipAdressTextArea.setBackground(buttonBox.getBackground());
            portTextArea.setEditable(false);
            portTextArea.setBackground(buttonBox.getBackground());
            nickNameTextArea.setEditable(false);
            nickNameTextArea.setBackground(buttonBox.getBackground());
            connectButton.setEnabled(false);
        }

    }
}