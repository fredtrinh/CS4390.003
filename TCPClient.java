import java.io.*;
import java.net.*;
import java.util.Random;

class TCPClient {

    public static void main(String argv[]) throws Exception {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter your name to connect to the Math Server:");
        String clientName = inFromUser.readLine();

        Socket clientSocket = new Socket("127.0.0.1", 5000);                                                                // TCP connection to server
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());                                // Send data to server
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));             // Receive data from server

        
        outToServer.writeBytes("NAME: " + clientName + '\n');                                                               // Send client name to server
        System.out.println("Connected to the server as " + clientName);

        
        String response = inFromServer.readLine();                                                                          // Receive acknowledgement from server
        System.out.println("Server: " + response);

        
        Random rand = new Random();                                                                                         // Send 3 random math requests to server
        String[] operations = {"ADD", "SUB", "MUL", "DIV"};
        for (int i = 0; i < 3; i++) {
            int a = rand.nextInt(10) + 1;                                                                                   // First random number
            int b = rand.nextInt(10) + 1;                                                                                   // Second random number
            String operation = operations[rand.nextInt(operations.length)];                                                 // Random operation
            String message = operation + " " + a + " " + b;

            outToServer.writeBytes(message + '\n');                                                                         // Send math operation to server
            System.out.println("Sent: " + message);

            response = inFromServer.readLine();                                                                             // Print server response
            System.out.println("Server: " + response);

            
            Thread.sleep(rand.nextInt(5000) + 1000);                                                                        // Random pause between requests
        }

        
        System.out.println("Exiting...");                                                                                   // Close connection
        outToServer.writeBytes("QUIT\n");                                                                                   // Send quit command to server
        clientSocket.close();                                                                                               // Close socket
        System.out.println("Connection closed.");
    }
}
