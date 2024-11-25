import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.BorderLayout;
import javax.swing.*;

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

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new QuizHandler(socket)).start();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    // Question class to encapsulate question data
    private static class Question {
        private String questionText;
        private String[] options;
        private int correctAnswer;

        public Question(String questionText, String[] options, int correctAnswer) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestionText() {
            return questionText;
        }

        public int getCorrectAnswer() {
            return correctAnswer;
        }

        public String formatForClient() {
            return questionText +  String.join("", options);
        }
    }

    private class QuizHandler implements Runnable {
        private Socket socket;
    
        public QuizHandler(Socket socket) {
            this.socket = socket;
        }
    
        public void run() {
            try {
                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
    
                // List of questions
                List<Question> questions = Arrays.asList(
                    new Question("What is the capital of France?", new String[]{"\n1. Paris\n", "2. Berlin\n", "3. Madrid\n", "4. Rome\n"}, 1),
                    new Question("What is 62 + 93?", new String[]{"\n1. 83\n", "2. 152\n", "3. 140\n", "4. 158\n"}, 2),
                    new Question("At Oakland University, What is The Computer Networks Course'?", new String[]{"\n1. CSI 2470\n", "2. CSI 2300\n", "3.CSI 3370\n", "4.CSI 1000\n"}, 1),
                    new Question("What Internet Protocol is TCP apart of?", new String[]{"\n1. Application Protocol", "\n2. Network Protocol", "\n3. Transport Protocol", "\n4. Link Protocol\n"}, 3),
                    new Question("What is the chemical symbol for gold?", new String[]{"\n1. Ag", "\n2. Gd", "\n3. Au", "\n4. G"}, 3),
                    new Question("What does IP address stand for?", new String[]{"\n1. Intellectual Property", "\n2. Internet Port", "\n3. Internet Protocol", "\n4. Internet Procedure"}, 3),
                    new Question("What planet is known as the Red Planet and is the fourth furthest planet from the sun?", new String[]{"\n1. Venus", "\n2. Mars", "\n3. Jupiter", "\n4. Saturn"}, 2)
                );
    
                int correctAnswers = 0;

                for (int i = 0; i < questions.size(); i++) {
                    Question question = questions.get(i);
    
                    // Send the question to the client
                    outputToClient.writeUTF(question.formatForClient());
                    outputToClient.flush(); // Ensure data is sent immediately
    
                    // Log the sent question
                    jta.append("Sent Question: " + question.getQuestionText() + "\n");
    
                    // Wait for the client's answer
                    int clientAnswer = inputFromClient.readInt();

                    if (clientAnswer == question.getCorrectAnswer()) {
                        correctAnswers++;
                    }
    
                    // Check the answer
                    String response = (clientAnswer == question.getCorrectAnswer()) ? "Correct!" : "Incorrect.";
                    outputToClient.writeUTF(response);
    
                    // Log the client's response
                    jta.append("Client's Answer: " + clientAnswer + " (" + response + ")\n");
                }
    
                // Notify client the quiz is complete
                outputToClient.writeUTF("Quiz Complete! You got " + correctAnswers + " out of " + questions.size() + " correct.");
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
