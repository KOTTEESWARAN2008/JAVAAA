import java.io.*;
import java.net.*;

public class FileServer {
    private static final int PORT = 5000; // You can change this port number


    public static void main(String[] args) {
        System.out.println("Server is starting and waiting for connections...");
       
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                     DataInputStream dis = new DataInputStream(socket.getInputStream());
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                   
                    System.out.println("Client connected: " + socket.getInetAddress());

                    // 1. Read the requested file name from the client
                    String fileName = dis.readUTF();
                    System.out.println("Client requested file: " + fileName);

                    // 2. Check if the file exists in the current folder
                    File file = new File(fileName);
                    if (file.exists() && !file.isDirectory()) {
                        // Send success status
                        dos.writeBoolean(true);
                        dos.writeLong(file.length());

                        // 3. Stream the file data to the client
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                dos.write(buffer, 0, bytesRead);
                            }
                        }
                        System.out.println("File '" + fileName + "' sent successfully.");
                    } else {
                        // Send failure status if file doesn't exist
                        dos.writeBoolean(false);
                        System.out.println("Requested file '" + fileName + "' does not exist.");
                    }
                } catch (IOException e) {
                    System.out.println("Error handling client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }
}