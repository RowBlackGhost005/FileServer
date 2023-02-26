package fileServerServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server Class that receive request from clients to send multiple files.
 * Default simultaneous clients is 5.
 *
 * @author Luis Angel Marin
 */
public class Server {

    //// SERVER CONFIG ////
    //Port to listen.
    private final int PORT = 4444;
    //Host of this server.
    private String host = "localhost";
    //Socket that handles the connections.
    private DatagramSocket serverSocket;
    //Packet that carries the information received.
    private DatagramPacket serverPacket;
    //Buffer of bytes[] that holds datagrampacket data.
    private byte[] buffer;
    //// SERVER CONFIG - end ////

    //Executor service for multi-threaded work.
    private ExecutorService executorService;
    
    public static void main(String[] args){
        
        Server server = new Server();
        
        server.executeServer();
    }

    /**
     * Configures and executes the server to start listening for requests.
     */
    public void executeServer(){

        //Server Initialization.
        try {
            serverSocket = new DatagramSocket(PORT, InetAddress.getByName(host));
            buffer = new byte[255];
            serverPacket = new DatagramPacket(buffer , buffer.length);
            executorService = Executors.newFixedThreadPool(5); //Sets how many clients can handle at time.
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        //Infinite loop to listen
        while(true) {

            try {
                serverSocket.receive(serverPacket);

                //Transform bytes[] into String.
                String received = new String(serverPacket.getData());

                //Prints the received information
                if(received != null)
                    System.out.println("Attending client from: " + serverPacket.getAddress().getHostAddress() + " - " + "Requested: " +received.trim());

                //Handle the request into a new thread.
                executorService.execute(new ServerProtocol(serverPacket));

                //Resets the buffer.
                buffer = new byte[255];
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
