import java.net.*;
import java.io.*;
import java.util.*;

import java.net.*;
import java.io.*;
import java.util.*;

class Server1 {
    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(5000);

        System.out.println("Waiting for client...");
        Socket s = ss.accept();
        System.out.println("Client Connected");

        Scanner sc = new Scanner(System.in);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream()));

        PrintWriter out = new PrintWriter(
                s.getOutputStream(), true);

        while (true) {

            // Server sends message
            System.out.print("Server: ");
            String msg = sc.nextLine();
            out.println(msg);

            if (msg.equalsIgnoreCase("bye"))
                break;

            // Receive Client message
            String reply = in.readLine();
            System.out.println("Client: " + reply);

            if (reply.equalsIgnoreCase("bye"))
                break;
        }

        s.close();
        ss.close();
    }
}