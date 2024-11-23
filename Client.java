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
            // Connect to the server
            Socket socket = new Socket("localhost", 8000);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());

            // Create a thread to listen for questions from the server
            new Thread(() -> {
                try {
                    while (true) {
                        // Receive question data
                        String questionData = fromServer.readUTF();

                        // Check if it's the end of the quiz
                        if (questionData.equals("Quiz Complete! Thank you for participating.")) {
                            jta.append("\n" + questionData + "\n");
                            break; // Exit the loop
                        }

                        // Split the question and options
                        String[] parts = questionData.split(";");
                        if (parts.length < 2) {
                            jta.append("" + questionData + "\n");
                            continue; // Skip to the next iteration
                        }

                        String question = parts[0];
                        String[] options = parts[1].split(",");

                        // Display the question
                        jta.append("\n" + question + "\n");

                        // Display each option with a number (1-4)
                        for (int i = 0; i < options.length; i++) {
                            jta.append((i + 1) + ": " + options[i] + "\n");
                        }
                    }
                } catch (IOException ex) {
                    jta.append("Error receiving data from server: " + ex.getMessage() + "\n");
                }
            }).start();
        } catch (IOException ex) {
            jta.append("Error connecting to server: " + ex.getMessage() + '\n');
        }
    }

    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                String input = jtf.getText().trim();
                if (!input.matches("[1-4]")) {
                    jta.append("Invalid input. Please enter a number between 1 and 4.\n");
                    return;
                }

                int choice = Integer.parseInt(input);
                toServer.writeInt(choice); // Send answer to server
                toServer.flush();

                // Receive and display the server's response
                String response = fromServer.readUTF();
                jta.append("Server response: " + response + "\n");

                // Clear the text field for the next input
                jtf.setText("");
            } catch (IOException ex) {
                jta.append("Error sending data to server: " + ex.getMessage() + "\n");
            }
        }
    }
}
