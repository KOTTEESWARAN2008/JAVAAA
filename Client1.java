import java.net.*;
import java.io.*;
import java.util.*;

class Client1 {
    public static void main(String[] args) throws Exception {

        Socket s = new Socket("localhost", 5000);

        Scanner sc = new Scanner(System.in);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream()));

        PrintWriter out = new PrintWriter(
                s.getOutputStream(), true);

        while (true) {

            // Receive Server message
            String msg = in.readLine();
            System.out.println("Server: " + msg);

            if (msg.equalsIgnoreCase("bye"))
                break;

            // Client replies
            System.out.print("Client: ");
            String reply = sc.nextLine();
            out.println(reply);

            if (reply.equalsIgnoreCase("bye"))
                break;
        }

        s.close();
    }
}