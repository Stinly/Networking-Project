import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.util.Date;

public class Server extends JFrame {
    private JTextArea jta = new JTextArea();

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        setLayout(new BorderLayout());
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(8000)) {
            jta.append("Server started at " + new Date() + '\n');

            Socket socket = serverSocket.accept();

            DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
            DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

            // Define the question and answers
            String question = "What is 30 + 63?";
            String[] options = { "76", "93", "6036", "69" };
            int correctAnswer = 2; // Index of the correct answer (1-based)

            // Send the question and options to the client
            String questionData = question + ";" + String.join(",", options);
            outputToClient.writeUTF(questionData);

            // Receive the client's answer
            int clientAnswer = inputFromClient.readInt();

            // Determine if the answer is correct
            String response = (clientAnswer == correctAnswer) ? "Correct!" : "Incorrect.";
            outputToClient.writeUTF(response);

            jta.append("Question sent: " + question + "\n");
            jta.append("Client's Answer: " + clientAnswer + " (" + response + ")\n");
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
