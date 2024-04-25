import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

class TCPServer {

    private static final int PORT = 5000;                                                                                                               // Port number
    private static Map<String, ConnectionHandler> activeClients = new ConcurrentHashMap<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);                                                                              // Four clients, max

    public static void main(String argv[]) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is UP and running on port " + PORT);

        while (true) {                                                                                                                                  // Always accept new clients
            Socket clientSocket = serverSocket.accept();
            ConnectionHandler clientThread = new ConnectionHandler(clientSocket);
            pool.execute(clientThread);
        }
    }

    private static class ConnectionHandler implements Runnable {
        private long loginTime;
        private Socket clientSocket;
        private String clientName;

        public ConnectionHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));                                 // Input socket
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());                                                    // Output socket

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");                                                             // Current local date and time formatting

                String line = inFromClient.readLine();                                                                                                  // First connection
                if (line != null && line.startsWith("NAME: ")) {                                                                                        // Read in client name
                    this.loginTime = System.currentTimeMillis();                                                                                        // Store client login time
                    this.clientName = line.substring(6);                                                                                                // Store client name
                    activeClients.put(clientName, this);
                    System.out.println(dtf.format(LocalDateTime.now()) + " | Client " + clientName + " connected.");
                    outToClient.writeBytes("Welcome " + clientName + "\n");
                }
                
                while ((line = inFromClient.readLine()) != null) {                                                                                      // Handle requests from client
                    if ("QUIT".equalsIgnoreCase(line)) {
                        break;
                    }

                    String response = processRequest(line);                                                                                             // Process request
                    System.out.println("[" + clientName + "]\tRequest processed: " + line);
                    outToClient.writeBytes(response + "\n");
                    System.out.println("[" + clientName + "]\tResponse sent: \"" + response + "\"");
                }

                clientSocket.close();                                                                                                                   // Close connection
                activeClients.remove(clientName);
                int connectionTime = (int) ((System.currentTimeMillis() - loginTime) / 1000);                                                           // Total time connected
                System.out.println(dtf.format(LocalDateTime.now()) + " | Client " + clientName + " disconnected. (" + connectionTime + " s)");
            } catch (IOException e) {
                System.err.println("Connection error with client: " + clientName);
            }
        }

        private String processRequest(String request) {                                                                                                 // Handles basic calculations
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                int num1 = Integer.parseInt(parts[1]);
                int num2 = Integer.parseInt(parts[2]);

                switch (parts[0].toUpperCase()) {
                    case "ADD":
                        return "Result: " + (num1 + num2);
                    case "SUB":
                        return "Result: " + (num1 - num2);
                    case "MUL":
                        return "Result: " + (num1 * num2);
                    case "DIV":                                                                                                                         // Division by zero case
                        if (num2 != 0) {
                            return "Result: " + (num1 / num2);
                        } else {
                            return "Error: Division by zero";
                        }
                    default:
                        return "Error: Invalid command";
                }
            } else {
                return "Error: Invalid request format";
            }
        }
    }
}
