import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) {
        String serverIP = "192.168.17.183"; // Put your computer's IP here
        int port = 5000;

        try (Socket socket = new Socket(serverIP, port)) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter file name to request: ");
            String fileName = scanner.nextLine();

            // Send file name to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(fileName);

            // Read response from server
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();

            if ("EXISTS".equals(response)) {
                InputStream in = socket.getInputStream();
                FileOutputStream fileOutput = new FileOutputStream("received_" + fileName);
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fileOutput.write(buffer, 0, bytesRead);
                }
                fileOutput.close();
                System.out.println("File downloaded successfully as: received_" + fileName);
            } else {
                System.out.println("Error: File does not exist on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
