import java.net.*;
import java.io.*;
import java.util.*;

class ChatServer {
    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(5000);

        System.out.println("Waiting for client...");
        Socket s = ss.accept();
        System.out.println("Client connected!");

        Scanner sc = new Scanner(System.in);
        DataInputStream in = new DataInputStream(s.getInputStream());
        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        while (true) {

            String msg = in.readUTF();
            System.out.println("Friend: " + msg);

            if (msg.equalsIgnoreCase("bye"))
                break;

            System.out.print("You: ");
            String reply = sc.nextLine();
            out.writeUTF(reply);

            if (reply.equalsIgnoreCase("bye"))
                break;
        }

        s.close();
        ss.close();
    }
}