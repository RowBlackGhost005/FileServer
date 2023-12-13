package rbg.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lamar
 */
public class test {
    
    public static void main(String[] args){
        try {
            byte[] buffer = new byte[255];
            DatagramSocket clientSocket = new DatagramSocket(4446 , InetAddress.getByName("localhost"));
            DatagramPacket clientPacket = new DatagramPacket(buffer , buffer.length , InetAddress.getByName("localhost") , 4444);
            clientSocket.send(clientPacket);
        } catch (UnknownHostException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
