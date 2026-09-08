import java.net.*;
import java.io.*;
import java.util.*;

class ChatClient {
    public static void main(String[] args) throws Exception {

        Socket s = new Socket("192.168.1.10", 5000);

        Scanner sc = new Scanner(System.in);

        DataInputStream in =
            new DataInputStream(s.getInputStream());

        DataOutputStream out =
            new DataOutputStream(s.getOutputStream());

        while (true) {

            // Send message
            System.out.print("You: ");
            String msg = sc.nextLine();
            out.writeUTF(msg);

            if (msg.equalsIgnoreCase("bye"))
                break;

            // Receive server reply
            String reply = in.readUTF();
            System.out.println("Server: " + reply);

            if (reply.equalsIgnoreCase("bye"))
                break;
        }

        s.close();
        sc.close();
    }
}