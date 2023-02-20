package fileServerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    
    public static void main(String[] args){
        
        Client client = new Client();
        
        client.connectServer();
    }
    
    public void connectServer(){
        
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            socket = new Socket("localhost", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;
            
            out.println("Ping!");
            
            String userInput = "";
            
            while(!userInput.equalsIgnoreCase("close")){
                
                userInput = scanner.nextLine();
                
                out.println(userInput);
            }
            
            //Communication with server
//            while ((fromServer = in.readLine()) != null) {
//                
//                System.out.println("Server: " + fromServer);
//            }
//            
            out.close();
            in.close();
            stdIn.close();
            socket.close();            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
