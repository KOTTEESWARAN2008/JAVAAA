import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;

public class FileClient {
    public static void main(String[] args) {
        String serverIP = args.length > 0 ? args[0] : "localhost";
        int port = 5000;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter full path of file to send: ");
            String input = reader.readLine().trim().replace("\"", "");
            Path file = Paths.get(input).toAbsolutePath().normalize();

            if (!Files.isRegularFile(file)) {
                System.out.println("File not found: " + file);
                return;
            }

            Path compressed = Files.createTempFile("file-transfer-", ".gz");
            try {
                try (InputStream in = Files.newInputStream(file);
                     OutputStream out = new GZIPOutputStream(Files.newOutputStream(compressed))) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                }

                long compressedSize = Files.size(compressed);
                try (Socket socket = new Socket(serverIP, port);
                     DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                     DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                     InputStream compressedIn = Files.newInputStream(compressed)) {

                    out.writeUTF(file.getFileName().toString());
                    out.writeLong(compressedSize);
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = compressedIn.read(buffer)) != -1) out.write(buffer, 0, n);
                    out.flush();

                    boolean success = in.readBoolean();
                    String message = in.readUTF();
                    System.out.println(message);
                    if (success) {
                        System.out.println("Original file size: " + Files.size(file) + " bytes");
                        System.out.println("Compressed transfer size: " + compressedSize + " bytes");
                        System.out.println("Open http://localhost:8080 to preview and download received files.");
                    }
                }
            } finally {
                Files.deleteIfExists(compressed);
            }
        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
            System.out.println("Make sure FileServer is running and port 5000 is available.");
        }
    }
}
