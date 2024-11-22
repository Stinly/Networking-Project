import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame {
    private JTextArea jta = new JTextArea();
    private JTextField jtf = new JTextField();

    private DataOutputStream toServer;
    private DataInputStream fromServer;

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel("Enter choice (1-4):"), BorderLayout.WEST);
        p.add(jtf, BorderLayout.CENTER);
        jtf.setHorizontalAlignment(JTextField.RIGHT);

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        jtf.addActionListener(new ButtonListener());

        setTitle("Client");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        try {
            Socket socket = new Socket("localhost", 8000);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());

            // Receive the question and options from the server
            String questionData = fromServer.readUTF();
            String[] parts = questionData.split(";");
            String question = parts[0];
            String[] options = parts[1].split(",");

            jta.append(question + "\n");
            for (int i = 0; i < options.length; i++) {
                jta.append((i + 1) + ": " + options[i] + "\n");
            }
        } catch (IOException ex) {
            jta.append(ex.toString() + '\n');
        }
    }

    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                // Send the client's choice to the server
                int choice = Integer.parseInt(jtf.getText().trim());
                toServer.writeInt(choice);
                toServer.flush();

                // Receive the response from the server
                String response = fromServer.readUTF();
                jta.append("Server response: " + response + "\n");

                // Disable further input after one question
                jtf.setEditable(false);
            } catch (IOException ex) {
                jta.append("Error: " + ex.getMessage() + "\n");
            }
        }
    }
}
