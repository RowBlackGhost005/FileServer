package fileServerServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class serverProtocol implements Runnable {
    
    PrintWriter out = null;
    BufferedReader in = null;
    Socket socket = null;
    
    public serverProtocol(Socket socket) throws IOException{
        this.socket = socket;

        this.out = null;
        this.in = null;

        out = new PrintWriter(this.socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(
                this.socket.getInputStream()));
    }

    @Override
    public void run() {
        
        try {
            
            String inputLine, outputLine;

            out.println(in.readLine());
            
            
            out.close();
            in.close();
            socket.close();   
            
        } catch (IOException ex) {
            Logger.getLogger(serverProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
