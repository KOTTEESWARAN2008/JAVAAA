import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    // Replace this string with your neighbor's computer's actual IP address
    private static final String SERVER_IP = "YOUR_SERVER_IP_ADDRESS";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name of the file you want to request: ");
        String requestedFile = scanner.nextLine();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            System.out.println("Connected to the server. Requesting file...");

            // 1. Send the requested filename to the server
            dos.writeUTF(requestedFile);

            // 2. Check if the server found the file
            boolean fileExists = dis.readBoolean();

            if (fileExists) {
                long fileSize = dis.readLong();
                System.out.println("File found. Size: " + fileSize + " bytes. Downloading...");

                // 3. Receive and save the file in the client's current folder
                try (FileOutputStream fos = new FileOutputStream("downloaded_" + requestedFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;

                    // Read only up to the file size specified by the server
                    while (totalBytesRead < fileSize &&
                          (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }
                }
                System.out.println("Download complete! Saved as: downloaded_" + requestedFile);
            } else {
                System.out.println("Error: The server could not find the file '" + requestedFile + "'.");
            }

        } catch (IOException e) {
            System.err.println("Client exception: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}